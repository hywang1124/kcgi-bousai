import { useEffect, useMemo, useRef, useState } from 'react'
import { useTranslation } from 'react-i18next'
import maplibregl from 'maplibre-gl'
import 'maplibre-gl/dist/maplibre-gl.css'
import type { Feature, Geometry } from 'geojson'
import { fetchShelters } from '../api/shelters'
import { fetchHazardZones } from '../api/hazardZones'
import type { HazardZone, Shelter } from '../api/types'
import { haversineKm, isValidLngLat, pointInGeometry } from '../lib/geo'

const CENTER: [number, number] = [135.768, 35.011]

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

function localizedName(
  item: { nameJa: string; nameEn: string | null; nameZh: string | null },
  lang: string,
): string {
  if (lang.startsWith('en') && item.nameEn) return item.nameEn
  if (lang.startsWith('zh') && item.nameZh) return item.nameZh
  return item.nameJa
}

interface ZoneGeom {
  zone: HazardZone
  geom: Geometry
}

type SortMode = 'default' | 'capacity' | 'distance'

interface UserLocation {
  lat: number
  lng: number
}

/** 避難所一覧（左）と防災マップ（右）の連動ページ。
 *  避難所をクリックすると地図が移動し、その地点を含む危険区域がハイライトされる。 */
export function SheltersMapPage() {
  const { t, i18n } = useTranslation()
  const lang = i18n.language

  const [shelters, setShelters] = useState<Shelter[]>([])
  const [zones, setZones] = useState<HazardZone[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [selectedId, setSelectedId] = useState<number | null>(null)
  const [sortMode, setSortMode] = useState<SortMode>('default')
  const [userLocation, setUserLocation] = useState<UserLocation | null>(null)
  const [locating, setLocating] = useState(false)
  const [geoError, setGeoError] = useState<string | null>(null)

  const containerRef = useRef<HTMLDivElement | null>(null)
  const markersRef = useRef<Map<number, maplibregl.Marker>>(new Map())
  const highlightedRef = useRef<number[]>([])
  const applyRef = useRef<((id: number) => void) | null>(null)
  const selectedRef = useRef<number | null>(null)

  // GeoJSON をパースしておく
  const zoneGeoms = useMemo<ZoneGeom[]>(() => {
    return zones.flatMap((zone) => {
      try {
        return [{ zone, geom: JSON.parse(zone.geojson) as Geometry }]
      } catch {
        return []
      }
    })
  }, [zones])

  // 避難所ごとに、含まれる危険区域を求める（バッジ表示と高亮に使用）
  const sheltersZones = useMemo<Map<number, HazardZone[]>>(() => {
    const map = new Map<number, HazardZone[]>()
    for (const s of shelters) {
      const inside = isValidLngLat(s.lng, s.lat)
        ? zoneGeoms.filter((zg) => pointInGeometry(s.lng, s.lat, zg.geom)).map((zg) => zg.zone)
        : []
      map.set(s.id, inside)
    }
    return map
  }, [shelters, zoneGeoms])

  // ユーザー位置からの距離（km）
  const distances = useMemo<Map<number, number>>(() => {
    const map = new Map<number, number>()
    if (!userLocation) return map
    for (const s of shelters) {
      if (isValidLngLat(s.lng, s.lat)) {
        map.set(s.id, haversineKm(userLocation.lat, userLocation.lng, s.lat, s.lng))
      }
    }
    return map
  }, [shelters, userLocation])

  // 並べ替え後の一覧
  const displayedShelters = useMemo<Shelter[]>(() => {
    const arr = [...shelters]
    if (sortMode === 'capacity') {
      arr.sort((a, b) => (b.capacity ?? -1) - (a.capacity ?? -1))
    } else if (sortMode === 'distance' && userLocation) {
      arr.sort((a, b) => (distances.get(a.id) ?? Infinity) - (distances.get(b.id) ?? Infinity))
    }
    return arr
  }, [shelters, sortMode, userLocation, distances])

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
    if (userLocation) {
      setSortMode('distance')
    } else {
      requestLocation()
    }
  }

  // データ取得
  useEffect(() => {
    let active = true
    Promise.all([fetchShelters(), fetchHazardZones()])
      .then(([s, z]) => {
        if (!active) return
        setShelters(s)
        setZones(z)
      })
      .catch((e: unknown) => {
        if (active) setError(e instanceof Error ? e.message : String(e))
      })
      .finally(() => {
        if (active) setLoading(false)
      })
    return () => {
      active = false
    }
  }, [])

  // 地図構築（言語・データが変わったら作り直す）
  useEffect(() => {
    if (!containerRef.current || shelters.length === 0) return
    let cancelled = false

    const map = new maplibregl.Map({
      container: containerRef.current,
      style: OSM_STYLE,
      center: CENTER,
      zoom: 12,
    })
    map.addControl(new maplibregl.NavigationControl(), 'top-right')

    map.on('load', () => {
      if (cancelled) return

      const features: Feature[] = zoneGeoms.map((zg) => ({
        type: 'Feature',
        id: zg.zone.id,
        geometry: zg.geom,
        properties: { name: localizedName(zg.zone, lang), severity: zg.zone.severity },
      }))
      map.addSource('hazard', { type: 'geojson', data: { type: 'FeatureCollection', features } })
      map.addLayer({
        id: 'hazard-fill',
        type: 'fill',
        source: 'hazard',
        paint: {
          'fill-color': [
            'match', ['get', 'severity'],
            'HIGH', '#e53935', 'MEDIUM', '#fb8c00', 'LOW', '#fdd835', '#9e9e9e',
          ],
          'fill-opacity': ['case', ['boolean', ['feature-state', 'highlight'], false], 0.6, 0.25],
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

      markersRef.current = new Map()
      for (const s of shelters) {
        if (!isValidLngLat(s.lng, s.lat)) continue
        const marker = new maplibregl.Marker({ color: '#1565c0' })
          .setLngLat([s.lng, s.lat])
          .setPopup(new maplibregl.Popup().setText(localizedName(s, lang)))
          .addTo(map)
        markersRef.current.set(s.id, marker)
      }

      // ユーザー現在地（緑マーカー）
      if (userLocation && isValidLngLat(userLocation.lng, userLocation.lat)) {
        new maplibregl.Marker({ color: '#2e7d32' })
          .setLngLat([userLocation.lng, userLocation.lat])
          .addTo(map)
        if (selectedRef.current == null) {
          map.flyTo({ center: [userLocation.lng, userLocation.lat], zoom: 13 })
        }
      }

      // 選択した避難所へ移動し、含まれる危険区域をハイライトする
      applyRef.current = (id: number) => {
        const shelter = shelters.find((x) => x.id === id)
        if (!shelter) return
        if (isValidLngLat(shelter.lng, shelter.lat)) {
          map.flyTo({ center: [shelter.lng, shelter.lat], zoom: 14 })
        }
        for (const zid of highlightedRef.current) {
          map.setFeatureState({ source: 'hazard', id: zid }, { highlight: false })
        }
        const containing = zoneGeoms
          .filter((zg) => isValidLngLat(shelter.lng, shelter.lat) && pointInGeometry(shelter.lng, shelter.lat, zg.geom))
          .map((zg) => zg.zone.id)
        for (const zid of containing) {
          map.setFeatureState({ source: 'hazard', id: zid }, { highlight: true })
        }
        highlightedRef.current = containing

        // 他の避難所のポップアップを閉じ、選択中のものだけ開く
        for (const [otherId, otherMarker] of markersRef.current) {
          const otherPopup = otherMarker.getPopup()
          if (otherId !== id && otherPopup && otherPopup.isOpen()) {
            otherMarker.togglePopup()
          }
        }
        const marker = markersRef.current.get(id)
        const popup = marker?.getPopup()
        if (marker && popup && !popup.isOpen()) {
          marker.togglePopup()
        }
      }

      // 再構築後、選択状態を復元
      if (selectedRef.current != null) {
        applyRef.current(selectedRef.current)
      }
    })

    return () => {
      cancelled = true
      applyRef.current = null
      map.remove()
    }
  }, [lang, shelters, zoneGeoms, userLocation])

  function handleSelect(id: number) {
    setSelectedId(id)
    selectedRef.current = id
    applyRef.current?.(id)
  }

  return (
    <section>
      <h2>{t('map.heading')}</h2>
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
            <button type="button" disabled={sortMode === 'capacity'} onClick={() => setSortMode('capacity')}>
              {t('sort.capacity')}
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

          {loading ? (
            <p>{t('shelters.loading')}</p>
          ) : shelters.length === 0 ? (
            <p>{t('shelters.empty')}</p>
          ) : (
            <ul className="shelter-list">
              {displayedShelters.map((s) => {
                const hazards = sheltersZones.get(s.id) ?? []
                const distance = distances.get(s.id)
                return (
                  <li key={s.id}>
                    <button
                      type="button"
                      className={`shelter-card-button${selectedId === s.id ? ' selected' : ''}`}
                      onClick={() => handleSelect(s.id)}
                    >
                      <span className="shelter-card-name">{localizedName(s, lang)}</span>
                      {s.capacity != null && (
                        <span className="shelter-card-meta">
                          {t('shelters.capacity')}: {s.capacity} {t('shelters.people')}
                        </span>
                      )}
                      {distance != null && (
                        <span className="shelter-card-meta">
                          {t('shelters.distance')}: {distance.toFixed(1)} km
                        </span>
                      )}
                      {hazards.length > 0 && (
                        <span className="hazard-badges">
                          {hazards.map((h) => (
                            <span key={h.id} className={`hazard-badge sev-${h.severity}`}>
                              {localizedName(h, lang)}
                            </span>
                          ))}
                        </span>
                      )}
                    </button>
                  </li>
                )
              })}
            </ul>
          )}
        </div>
        <div ref={containerRef} className="shelters-map-canvas" />
      </div>
    </section>
  )
}
