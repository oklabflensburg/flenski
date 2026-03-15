<script setup lang="ts">
const props = defineProps({
  visible: {
    type: Boolean,
    required: true
  }
});
const emit = defineEmits(['update:visible']);

function closeDialog() {
  emit('update:visible', false);
}
</script>

<template>

  <Dialog  :visible="visible" @update:visible="emit('update:visible', $event)" modal :dismissableMask="true" header="Was ist FlensKI?" :style="{ width: '60rem' }">
    <div class="prose lg:prose-xl">
      FlensKI is eine KI-gestützte Suche für offene Daten der Stadt Flensburg. Sie wird ehrenamtlich und unentgeldlich vom <a href="https://codefor.de/flensburg/" class="underline">OK Lab Flensburg</a> entwickelt und betrieben.

      <h3>Was macht FlensKI?</h3>
      Es sammelt offene Daten der Stadt Flensburg und beantwortet Fragen dazu. Beispielanfragen könnten sein:
      <ul>
        <li>"Was wurde in der letzten Ratsversammlung besprochen?"</li>
        <li>"Welche Beschlüsse gibt es zur Innenenstadt?"</li>
      </ul>

      <h3>Wie zuverlässig sind die Antworten?</h3>
      KIs können Fehler machen. Wir versuchen dies so weit wie möglich zu minimieren, aber es kann eine 100%ige Genauigkeit nicht garantiert werden. Zusätzlich zu KI-generierten Anwtworten werden die Quellen angezeigt, auf deren Basis die Antwort generiert wurde. Die KI-Antwort sollte also eher als Recherchehilfe betrachtet werden.
      Da dieses Projekt ein ehrenamtliches Projekt ist, dessen laufende Kosten privat gedeckt werden, gibt es keine Garantie für die Verfügbarkeit des Dienstes.

      <h3>Welche Daten werden erhoben und wo werden diese verarbeitet?</h3>
      Suchanfragen und die generierten Daten werden in Log-Files auf Servern in Deutschland gespeichert, um die Qualität der Software zu analysieren und zu verbessern.
      Für die Generierung der KI-Antworten nutzen wir ein Large Language Model des französischen Unternehmens "Mistral". Suchanfragen werden an die API von Mistral gesendet, um die KI-Antworten zu generieren.

      <h3>Fragen, Kommentare, Anregungen</h3>
      ...gerne per mail an hendrik(@)oklabflensburg.de

      <h2>Versionshistorie</h2>
      <h3>Version 0.1</h3>
      <ul>
        <li>enthält Dokumente der Ratsversammlung der Stadt Flensburg ab 2017</li>
        <li>aktualisiert laufend neue Dokumente der Ratsversammlung</li>
        <li>wandelt Benutzerfragen in Suchanfragen um, führt eine hybride Suche durch und und generiert eine Antwort auf Basis der gefundenen Dokumente</li>
        <li>Zeigt zusätzlich die Dokumente an, auf deren Basis die KI-Antwort generiert wurde</li>
        <li>Erkennt wenn Fragen sich auf eine bestimmte Zeit beziehen und filtert die Dokumente entsprechend</li>
      </ul>
    </div>
  </Dialog>
</template>
