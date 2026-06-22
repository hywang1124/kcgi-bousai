import { useEffect, useState } from 'react'
import type { FormEvent } from 'react'
import { useTranslation } from 'react-i18next'
import { createShelter, deleteShelter, fetchShelters, updateShelter } from '../api/shelters'
import type { ShelterInput } from '../api/shelters'
import type { Shelter } from '../api/types'

interface Props {
  token: string
}

interface FormState {
  nameJa: string
  nameEn: string
  nameZh: string
  address: string
  lat: string
  lng: string
  capacity: string
  facilities: string
}

const EMPTY: FormState = {
  nameJa: '',
  nameEn: '',
  nameZh: '',
  address: '',
  lat: '',
  lng: '',
  capacity: '',
  facilities: '',
}

function toInput(form: FormState): ShelterInput {
  return {
    nameJa: form.nameJa,
    nameEn: form.nameEn.trim() || null,
    nameZh: form.nameZh.trim() || null,
    address: form.address.trim() || null,
    lat: Number(form.lat),
    lng: Number(form.lng),
    capacity: form.capacity.trim() ? Number(form.capacity) : null,
    facilities: form.facilities
      ? form.facilities.split(',').map((s) => s.trim()).filter(Boolean)
      : [],
  }
}

/** 避難所の作成・更新・削除（ADMIN）。 */
export function ShelterAdmin({ token }: Props) {
  const { t } = useTranslation()
  const [shelters, setShelters] = useState<Shelter[]>([])
  const [form, setForm] = useState<FormState>(EMPTY)
  const [editingId, setEditingId] = useState<number | null>(null)
  const [error, setError] = useState<string | null>(null)

  function reload() {
    fetchShelters()
      .then(setShelters)
      .catch((e: unknown) => setError(e instanceof Error ? e.message : String(e)))
  }

  useEffect(() => {
    reload()
  }, [])

  function resetForm() {
    setForm(EMPTY)
    setEditingId(null)
  }

  async function handleSubmit(e: FormEvent) {
    e.preventDefault()
    setError(null)
    try {
      const input = toInput(form)
      if (editingId == null) {
        await createShelter(token, input)
      } else {
        await updateShelter(token, editingId, input)
      }
      resetForm()
      reload()
    } catch (err: unknown) {
      setError(err instanceof Error ? err.message : String(err))
    }
  }

  function handleEdit(s: Shelter) {
    setEditingId(s.id)
    setForm({
      nameJa: s.nameJa,
      nameEn: s.nameEn ?? '',
      nameZh: s.nameZh ?? '',
      address: s.address ?? '',
      lat: String(s.lat),
      lng: String(s.lng),
      capacity: s.capacity != null ? String(s.capacity) : '',
      facilities: s.facilities.join(', '),
    })
  }

  async function handleDelete(id: number) {
    setError(null)
    try {
      await deleteShelter(token, id)
      if (editingId === id) resetForm()
      reload()
    } catch (err: unknown) {
      setError(err instanceof Error ? err.message : String(err))
    }
  }

  return (
    <div>
      <h3>{editingId == null ? t('admin.shelterAdmin.create') : t('admin.shelterAdmin.edit')}</h3>
      <form onSubmit={handleSubmit} className="admin-form">
        <input
          placeholder={t('admin.shelterAdmin.nameJa')}
          value={form.nameJa}
          onChange={(e) => setForm({ ...form, nameJa: e.target.value })}
        />
        <input
          placeholder={t('admin.shelterAdmin.nameEn')}
          value={form.nameEn}
          onChange={(e) => setForm({ ...form, nameEn: e.target.value })}
        />
        <input
          placeholder={t('admin.shelterAdmin.nameZh')}
          value={form.nameZh}
          onChange={(e) => setForm({ ...form, nameZh: e.target.value })}
        />
        <input
          placeholder={t('shelters.address')}
          value={form.address}
          onChange={(e) => setForm({ ...form, address: e.target.value })}
        />
        <input
          placeholder={t('admin.shelterAdmin.lat')}
          value={form.lat}
          onChange={(e) => setForm({ ...form, lat: e.target.value })}
        />
        <input
          placeholder={t('admin.shelterAdmin.lng')}
          value={form.lng}
          onChange={(e) => setForm({ ...form, lng: e.target.value })}
        />
        <input
          placeholder={t('shelters.capacity')}
          value={form.capacity}
          onChange={(e) => setForm({ ...form, capacity: e.target.value })}
        />
        <input
          placeholder={t('admin.shelterAdmin.facilitiesHint')}
          value={form.facilities}
          onChange={(e) => setForm({ ...form, facilities: e.target.value })}
        />
        <div className="admin-form-actions">
          <button type="submit" disabled={!form.nameJa.trim() || !form.lat.trim() || !form.lng.trim()}>
            {t('admin.shelterAdmin.save')}
          </button>
          {editingId != null && (
            <button type="button" onClick={resetForm}>
              {t('admin.shelterAdmin.cancel')}
            </button>
          )}
        </div>
      </form>

      {error && <p role="alert">{error}</p>}

      <ul className="admin-list">
        {shelters.map((s) => (
          <li key={s.id}>
            <span>
              #{s.id} {s.nameJa}
            </span>
            <span className="admin-row-actions">
              <button type="button" onClick={() => handleEdit(s)}>
                {t('admin.shelterAdmin.edit')}
              </button>
              <button type="button" onClick={() => handleDelete(s.id)}>
                {t('admin.shelterAdmin.delete')}
              </button>
            </span>
          </li>
        ))}
      </ul>
    </div>
  )
}
