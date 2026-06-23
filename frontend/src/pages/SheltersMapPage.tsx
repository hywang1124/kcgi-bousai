import { useEffect, useMemo, useRef, useState } from 'react'
import { useTranslation } from 'react-i18next'
import maplibregl from 'maplibre-gl'
import 'maplibre-gl/dist/maplibre-gl.css'
import type { Feature, Geometry } from 'geojson'
import { fetchHazardZones } from '../api/hazardZones'
import type { HazardZone } from '../api/types'
import { SHELTERS } from '../data/shelters'
import type { HazardKind } from '../data/shelters'
import { haversineKm, isValidLngLat, pointInGeometry } from '../lib/geo'

const CENTER: [number, number] = [135.768, 35.011]
const HAZARD_ORDER: HazardKind[] = [
  'earthquake', 'flood', 'inlandFlood', 'landslide', 'tsunami', 'stormSurge', 'fire', 'volcano',
]

const OSM_STYLE: maplibregl.StyleSpecification = {
  version: 8,
  sources: {
    osm: {
      type: 'raster',
      tiles: ['https://tile.openstreetmap.org/{z}/{x}/{y}.png'],
      tileSize: 256,
      attribution: '© OpenStreetMap contributors',
    },
  },
  layers: [{ id: 'osm', type: 'raster', source: 'osm' }],
}

function localizedZoneName(z: HazardZone, lang: string): string {
  if (lang.startsWith('en') && z.nameEn) return z.nameEn
  if (lang.startsWith('zh') && z.nameZh) return z.nameZh
  return z.nameJa
}

interface ZoneGeom {
  zone: HazardZone
  geom: Geometry
}

type SortMode = 'default' | 'distance'
interface UserLocation {
  lat: number
  lng: number
}

/** 避難所一覧（左）＋防災マップ（右）。避難所は静的データ（国土地理院）から表示する。 */
export function SheltersMapPage() {
  const { t, i18n } = useTranslation()
  const lang = i18n.language

  const [zones, setZones] = useState<HazardZone[]>([])
  const [error, setError] = useState<string | null>(null)
  const [selectedId, setSelectedId] = useState<number | null>(null)
  const [sortMode, setSortMode] = useState<SortMode>('default')
  const [userLocation, setUserLocation] = useState<UserLocation | null>(null)
  const [locating, setLocating] = useState(false)
  const [geoError, setGeoError] = useState<string | null>(null)

  const containerRef = useRef<HTMLDivElement | null>(null)
  const mapRef = useRef<maplibregl.Map | null>(null)
  const popupRef = useRef<maplibregl.Popup | null>(null)
  const highlightedZonesRef = useRef<number[]>([])
  const prevShelterRef = useRef<number | null>(null)
  const applyRef = useRef<((id: number) => void) | null>(null)
  const selectedRef = useRef<number | null>(null)
  const pickRef = useRef<((id: number) => void) | null>(null)

  const zoneGeoms = useMemo<ZoneGeom[]>(() => {
    return zones.flatMap((zone) => {
      try {
        return [{ zone, geom: JSON.parse(zone.geojson) as Geometry }]
      } catch {
        return []
      }
    })
  }, [zones])

  const distances = useMemo<Map<number, number>>(() => {
    const map = new Map<number, number>()
    if (!userLocation) return map
    for (const s of SHELTERS) {
      if (isValidLngLat(s.lng, s.lat)) {
        map.set(s.id, haversineKm(userLocation.lat, userLocation.lng, s.lat, s.lng))
      }
    }
    return map
  }, [userLocation])

  const displayedShelters = useMemo(() => {
    if (sortMode === 'distance' && userLocation) {
      return [...SHELTERS].sort(
        (a, b) => (distances.get(a.id) ?? Infinity) - (distances.get(b.id) ?? Infinity),
      )
    }
    return SHELTERS
  }, [sortMode, userLocation, distances])

  // 地図構築
  useEffect(() => {
    if (!containerRef.current) return
    let cancelled = false
    prevShelterRef.current = null

    const map = new maplibregl.Map({
      container: containerRef.current,
      style: OSM_STYLE,
      center: CENTER,
      zoom: 12,
    })
    map.addControl(new maplibregl.NavigationControl(), 'top-right')
    mapRef.current = map

    map.on('load', () => {
      if (cancelled) return

      // 避難所（パフォーマンスのため円レイヤで描画）
      const shelterFeatures: Feature[] = SHELTERS.filter((s) => isValidLngLat(s.lng, s.lat)).map((s) => ({
        type: 'Feature',
        id: s.id,
        geometry: { type: 'Point', coordinates: [s.lng, s.lat] },
        properties: { name: s.name },
      }))
      map.addSource('shelters', { type: 'geojson', data: { type: 'FeatureCollection', features: shelterFeatures } })
      map.addLayer({
        id: 'shelters-circles',
        type: 'circle',
        source: 'shelters',
        paint: {
          'circle-radius': ['case', ['boolean', ['feature-state', 'selected'], false], 8, 4],
          'circle-color': ['case', ['boolean', ['feature-state', 'selected'], false], '#e53935', '#1565c0'],
          'circle-stroke-width': 1.5,
          'circle-stroke-color': '#ffffff',
        },
      })

      // 危険区域
      const zoneFeatures: Feature[] = zoneGeoms.map((zg) => ({
        type: 'Feature',
        id: zg.zone.id,
        geometry: zg.geom,
        properties: { name: localizedZoneName(zg.zone, lang), severity: zg.zone.severity },
      }))
      map.addSource('hazard', { type: 'geojson', data: { type: 'FeatureCollection', features: zoneFeatures } })
      map.addLayer({
        id: 'hazard-fill',
        type: 'fill',
        source: 'hazard',
        paint: {
          'fill-color': [
            'match', ['get', 'severity'],
            'HIGH', '#e53935', 'MEDIUM', '#fb8c00', 'LOW', '#fdd835', '#9e9e9e',
          ],
          'fill-opacity': ['case', ['boolean', ['feature-state', 'highlight'], false], 0.5, 0.2],
        },
      })
      map.addLayer({
        id: 'hazard-outline',
        type: 'line',
        source: 'hazard',
        paint: {
          'line-color': ['case', ['boolean', ['feature-state', 'highlight'], false], '#ffeb3b', '#b71c1c'],
          'line-width': ['case', ['boolean', ['feature-state', 'highlight'], false], 4, 1.5],
        },
      })

      // ユーザー現在地
      if (userLocation && isValidLngLat(userLocation.lng, userLocation.lat)) {
        new maplibregl.Marker({ color: '#2e7d32' })
          .setLngLat([userLocation.lng, userLocation.lat])
          .addTo(map)
        if (selectedRef.current == null) {
          map.flyTo({ center: [userLocation.lng, userLocation.lat], zoom: 13 })
        }
      }

      popupRef.current = new maplibregl.Popup({ closeButton: true, closeOnClick: false })

      map.on('click', 'shelters-circles', (e) => {
        const id = e.features?.[0]?.id
        if (id != null) pickRef.current?.(Number(id))
      })
      map.on('mouseenter', 'shelters-circles', () => {
        map.getCanvas().style.cursor = 'pointer'
      })
      map.on('mouseleave', 'shelters-circles', () => {
        map.getCanvas().style.cursor = ''
      })

      applyRef.current = (id: number) => {
        const sh = SHELTERS.find((s) => s.id === id)
        if (!sh) return

        if (prevShelterRef.current != null) {
          map.setFeatureState({ source: 'shelters', id: prevShelterRef.current }, { selected: false })
        }
        map.setFeatureState({ source: 'shelters', id }, { selected: true })
        prevShelterRef.current = id

        if (isValidLngLat(sh.lng, sh.lat)) {
          map.flyTo({ center: [sh.lng, sh.lat], zoom: 15 })
          popupRef.current?.setLngLat([sh.lng, sh.lat]).setText(sh.name).addTo(map)
        }

        for (const zid of highlightedZonesRef.current) {
          map.setFeatureState({ source: 'hazard', id: zid }, { highlight: false })
        }
        const containing = zoneGeoms
          .filter((zg) => isValidLngLat(sh.lng, sh.lat) && pointInGeometry(sh.lng, sh.lat, zg.geom))
          .map((zg) => zg.zone.id)
        for (const zid of containing) {
          map.setFeatureState({ source: 'hazard', id: zid }, { highlight: true })
        }
        highlightedZonesRef.current = containing
      }

      if (selectedRef.current != null) {
        applyRef.current(selectedRef.current)
      }
    })

    return () => {
      cancelled = true
      applyRef.current = null
      popupRef.current = null
      mapRef.current = null
      map.remove()
    }
  }, [lang, zoneGeoms, userLocation])

  function handleSelect(id: number) {
    setSelectedId(id)
    selectedRef.current = id
    applyRef.current?.(id)
    document.getElementById('shelter-' + id)?.scrollIntoView({ block: 'nearest' })
  }

  // 地図上のクリックから最新の handleSelect を呼べるように同期する
  useEffect(() => {
    pickRef.current = handleSelect
  })

  function requestLocation() {
    if (!('geolocation' in navigator)) {
      setGeoError(t('shelters.locationError'))
      return
    }
    setLocating(true)
    setGeoError(null)
    navigator.geolocation.getCurrentPosition(
      (pos) => {
        setUserLocation({ lat: pos.coords.latitude, lng: pos.coords.longitude })
        setSortMode('distance')
        setLocating(false)
      },
      () => {
        setGeoError(t('shelters.locationError'))
        setLocating(false)
      },
      { enableHighAccuracy: true, timeout: 10000 },
    )
  }

  function handleDistanceSort() {
    if (userLocation) setSortMode('distance')
    else requestLocation()
  }

  // データ取得（危険区域のみバックエンドから）
  useEffect(() => {
    let active = true
    fetchHazardZones()
      .then((z) => {
        if (active) setZones(z)
      })
      .catch((e: unknown) => {
        if (active) setError(e instanceof Error ? e.message : String(e))
      })
    return () => {
      active = false
    }
  }, [])

  return (
    <section>
      <h2>{t('map.heading')}</h2>
      <p className="home-lead">{t('shelters.count', { count: SHELTERS.length })}</p>
      {error && (
        <p role="alert">
          {t('shelters.error')}: {error}
        </p>
      )}

      <div className="shelters-map">
        <div className="shelters-map-list">
          <div className="sort-toolbar">
            <span>{t('sort.label')}:</span>
            <button type="button" disabled={sortMode === 'default'} onClick={() => setSortMode('default')}>
              {t('sort.default')}
            </button>
            <button
              type="button"
              disabled={(sortMode === 'distance' && !!userLocation) || locating}
              onClick={handleDistanceSort}
            >
              {t('sort.distance')}
            </button>
          </div>
          {locating && <p className="info">{t('shelters.locating')}</p>}
          {geoError && <p role="alert">{geoError}</p>}

          <ul className="shelter-list">
            {displayedShelters.map((s) => {
              const distance = distances.get(s.id)
              return (
                <li key={s.id}>
                  <button
                    type="button"
                    id={'shelter-' + s.id}
                    className={`shelter-card-button${selectedId === s.id ? ' selected' : ''}`}
                    onClick={() => handleSelect(s.id)}
                  >
                    <span className="shelter-card-name">{s.name}</span>
                    <span className="shelter-card-meta">{s.address}</span>
                    {distance != null && (
                      <span className="shelter-card-meta">
                        {t('shelters.distance')}: {distance.toFixed(1)} km
                      </span>
                    )}
                    {s.hazards.length > 0 && (
                      <span className="hazard-badges">
                        {HAZARD_ORDER.filter((h) => s.hazards.includes(h)).map((h) => (
                          <span key={h} className="type-badge">
                            {t(`hazardType.${h}`)}
                          </span>
                        ))}
                      </span>
                    )}
                  </button>
                </li>
              )
            })}
          </ul>
          <p className="data-source">{t('shelters.dataSource')}</p>
        </div>
        <div ref={containerRef} className="shelters-map-canvas" />
      </div>
    </section>
  )
}
