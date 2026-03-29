<script setup lang="ts">
import { computed } from 'vue'
import { storeToRefs } from 'pinia'
import { useSettingsStore } from '@/stores/settings'

defineProps({
  visibleSettings: {
    type: Boolean,
    required: true
  }
})
const emit = defineEmits(['update:visibleSettings'])

const searchTypes = [
  { label: 'Lexikalisch', value: 'lexical' },
  { label: 'Semantisch', value: 'semantic' },
  { label: 'Hybrid', value: 'hybrid' }
]

const settingsStore = useSettingsStore()
const { searchType, timeBoost, timeBoostMidPoint, timeBoostScale, limit, timeBoostDateField } = storeToRefs(settingsStore)

const fromDate = computed<Date | null>({
  get: () => settingsStore.fromSourceDateTime ? new Date(settingsStore.fromSourceDateTime) : null,
  set: (val: Date | null) => { settingsStore.fromSourceDateTime = val ? val.toISOString() : null }
})

const untilDate = computed<Date | null>({
  get: () => settingsStore.untilSourceDateTime ? new Date(settingsStore.untilSourceDateTime) : null,
  set: (val: Date | null) => { settingsStore.untilSourceDateTime = val ? val.toISOString() : null }
})
</script>

<template>
  <Dialog :visible="visibleSettings" @update:visible="emit('update:visibleSettings', $event)" modal :dismissableMask="true" header="Einstellungen" :style="{ width: '60rem' }">
    <form class="grid grid-cols-2 gap-6 py-2" @submit.prevent>

      <!-- Suchmodus -->
      <div class="flex flex-col gap-2">
        <label for="searchType" class="font-medium text-gray-700">Suchmodus</label>
        <Select
          id="searchType"
          v-model="searchType"
          :options="searchTypes"
          optionLabel="label"
          optionValue="value"
          placeholder="Suchmodus wählen"
          class="w-full"
        />
        <small class="text-gray-500">Wähle zwischen lexikaler, semantischer oder hybrider Suche.</small>
      </div>

      <!-- Time Boost aktivieren -->
      <div class="flex flex-col gap-2">
        <label class="font-medium text-gray-700">Time Boost</label>
        <div class="flex items-center gap-2">
          <Checkbox inputId="timeBoost" v-model="timeBoost" :binary="true" />
          <label for="timeBoost" class="cursor-pointer">Time Boost aktivieren</label>
        </div>
        <small class="text-gray-500">Neuere Dokumente werden bei der Suche höher gewichtet.</small>
      </div>

      <!-- Time Boost Midpoint -->
      <div class="flex flex-col gap-2">
        <label for="timeBoostMidPoint" class="font-medium text-gray-700">Time Boost Midpoint (Tage)</label>
        <InputNumber
          id="timeBoostMidPoint"
          v-model="timeBoostMidPoint"
          :min="0"
          :maxFractionDigits="2"
          class="w-full"
          placeholder="0 = Standardwert"
        />
        <small class="text-gray-500">Halbwertszeit des Zeitgewichtung in Tagen. 0 = Konfigurationsstandard.</small>
      </div>

      <!-- Time Boost Scale -->
      <div class="flex flex-col gap-2">
        <label for="timeBoostScale" class="font-medium text-gray-700">Time Boost Scale</label>
        <InputNumber
          id="timeBoostScale"
          v-model="timeBoostScale"
          :min="0"
          class="w-full"
          placeholder="0 = Standardwert"
        />
        <small class="text-gray-500">Skalierungsfaktor für den Time Boost. 0 = Konfigurationsstandard.</small>
      </div>

      <!-- Limit -->
      <div class="flex flex-col gap-2">
        <label for="limit" class="font-medium text-gray-700">Ergebnislimit</label>
        <InputNumber
          id="limit"
          v-model="limit"
          :min="0"
          class="w-full"
          placeholder="0 = Standardwert"
        />
        <small class="text-gray-500">Maximale Anzahl an Suchergebnissen. 0 = Konfigurationsstandard.</small>
      </div>

      <!-- Time Boost Date Field -->
      <div class="flex flex-col gap-2">
        <label for="timeBoostDateField" class="font-medium text-gray-700">Time Boost Datumsfeld</label>
        <InputText
          id="timeBoostDateField"
          v-model="timeBoostDateField"
          class="w-full"
          placeholder="leer = Standardwert"
        />
        <small class="text-gray-500">Name des Datumsfeldes für den Time Boost.</small>
      </div>

      <!-- Von Datum -->
      <div class="flex flex-col gap-2">
        <label for="fromDate" class="font-medium text-gray-700">Von Datum</label>
        <DatePicker
          id="fromDate"
          v-model="fromDate"
          showTime
          hourFormat="24"
          showButtonBar
          dateFormat="dd.mm.yy"
          class="w-full"
          placeholder="Kein Filter"
        />
        <small class="text-gray-500">Nur Dokumente ab diesem Datum berücksichtigen.</small>
      </div>

      <!-- Bis Datum -->
      <div class="flex flex-col gap-2">
        <label for="untilDate" class="font-medium text-gray-700">Bis Datum</label>
        <DatePicker
          id="untilDate"
          v-model="untilDate"
          showTime
          hourFormat="24"
          showButtonBar
          dateFormat="dd.mm.yy"
          class="w-full"
          placeholder="Kein Filter"
        />
        <small class="text-gray-500">Nur Dokumente bis zu diesem Datum berücksichtigen.</small>
      </div>

    </form>
  </Dialog>
</template>

