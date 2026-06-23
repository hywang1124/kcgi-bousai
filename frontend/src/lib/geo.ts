import type { Geometry } from 'geojson'

/** 点がリング（外周）の内側にあるか（レイキャスティング法）。ring: [lng, lat][] */
function pointInRing(lng: number, lat: number, ring: number[][]): boolean {
  let inside = false
  for (let i = 0, j = ring.length - 1; i < ring.length; j = i++) {
    const xi = ring[i][0]
    const yi = ring[i][1]
    const xj = ring[j][0]
    const yj = ring[j][1]
    const intersect =
      yi > lat !== yj > lat && lng < ((xj - xi) * (lat - yi)) / (yj - yi) + xi
    if (intersect) inside = !inside
  }
  return inside
}

/** 点が GeoJSON ジオメトリ（Polygon / MultiPolygon）の内側にあるか。 */
export function pointInGeometry(lng: number, lat: number, geom: Geometry): boolean {
  if (geom.type === 'Polygon') {
    return pointInRing(lng, lat, geom.coordinates[0])
  }
  if (geom.type === 'MultiPolygon') {
    return geom.coordinates.some((polygon) => pointInRing(lng, lat, polygon[0]))
  }
  return false
}

/** 緯度経度が有効か。 */
export function isValidLngLat(lng: number, lat: number): boolean {
  return Number.isFinite(lng) && Number.isFinite(lat) && Math.abs(lat) <= 90 && Math.abs(lng) <= 180
}
