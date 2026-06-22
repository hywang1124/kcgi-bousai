import { useEffect, useRef } from 'react'
import { useTranslation } from 'react-i18next'
import maplibregl from 'maplibre-gl'
import 'maplibre-gl/dist/maplibre-gl.css'
import type { Feature, Geometry } from 'geojson'
import { fetchShelters } from '../api/shelters'
import { fetchHazardZones } from '../api/hazardZones'
import type { HazardZone, Shelter } from '../api/types'

// 京都市中心付近
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

/** 防災マップ。OpenStreetMap 上に避難所マーカーと危険区域ポリゴンを表示する。 */
export function MapPage() {
  const { t, i18n } = useTranslation()
  const containerRef = useRef<HTMLDivElement | null>(null)

  // 言語が変わるとマーカー名も変わるため、言語をキーに再構築する
  useEffect(() => {
    if (!containerRef.current) return
    const lang = i18n.language
    let cancelled = false

    const map = new maplibregl.Map({
      container: containerRef.current,
      style: OSM_STYLE,
      center: CENTER,
      zoom: 12,
    })
    map.addControl(new maplibregl.NavigationControl(), 'top-right')

    map.on('load', () => {
      void (async () => {
        const [zones, shelters] = await Promise.all([
          fetchHazardZones().catch(() => [] as HazardZone[]),
          fetchShelters().catch(() => [] as Shelter[]),
        ])
        if (cancelled) return

        // 危険区域（GeoJSON ポリゴン）
        const features: Feature[] = zones.map((z) => ({
          type: 'Feature',
          geometry: JSON.parse(z.geojson) as Geometry,
          properties: { name: localizedName(z, lang), severity: z.severity },
        }))
        map.addSource('hazard', {
          type: 'geojson',
          data: { type: 'FeatureCollection', features },
        })
        map.addLayer({
          id: 'hazard-fill',
          type: 'fill',
          source: 'hazard',
          paint: {
            'fill-color': [
              'match',
              ['get', 'severity'],
              'HIGH', '#e53935',
              'MEDIUM', '#fb8c00',
              'LOW', '#fdd835',
              '#9e9e9e',
            ],
            'fill-opacity': 0.35,
          },
        })
        map.addLayer({
          id: 'hazard-outline',
          type: 'line',
          source: 'hazard',
          paint: { 'line-color': '#b71c1c', 'line-width': 1.5 },
        })

        // 危険区域クリックで名称をポップアップ
        map.on('click', 'hazard-fill', (e) => {
          const name = e.features?.[0]?.properties?.name
          if (name) {
            new maplibregl.Popup().setLngLat(e.lngLat).setText(String(name)).addTo(map)
          }
        })
        map.on('mouseenter', 'hazard-fill', () => {
          map.getCanvas().style.cursor = 'pointer'
        })
        map.on('mouseleave', 'hazard-fill', () => {
          map.getCanvas().style.cursor = ''
        })

        // 避難所マーカー（不正な座標はスキップして地図全体が壊れないようにする）
        for (const s of shelters) {
          if (
            !Number.isFinite(s.lat) || !Number.isFinite(s.lng) ||
            Math.abs(s.lat) > 90 || Math.abs(s.lng) > 180
          ) {
            continue
          }
          new maplibregl.Marker({ color: '#1565c0' })
            .setLngLat([s.lng, s.lat])
            .setPopup(new maplibregl.Popup().setText(localizedName(s, lang)))
            .addTo(map)
        }
      })()
    })

    return () => {
      cancelled = true
      map.remove()
    }
  }, [i18n.language])

  return (
    <section>
      <h2>{t('map.heading')}</h2>
      <p className="map-legend">
        <span className="legend-shelter">●</span> {t('map.shelters')}
        <span className="legend-hazard">■</span> {t('map.hazardZones')}
      </p>
      <div ref={containerRef} className="map-container" />
    </section>
  )
}
