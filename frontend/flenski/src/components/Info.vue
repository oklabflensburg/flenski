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
      <h1>FlensKI - ein KI-gestütztes Werkzeug für offene Daten der Stadt
        Flensburg</h1>

      <p>Was tut FlensKI?</p>
      <p>Es sammelt offene Daten der Stadt Flensburg und beantwortet Fragen dazu.
        Beispiele für solche Fragen könnten sein: "Welche
        Themen wurden in der letzten Ratsversammlung diskutiert?" oder "Welche
        Beschlüsse gibt es zur Innenstadt-Entwicklung?"</p>

      <h2>Wie zuverlässig sind die Antworten?</h2>
      <p>Künstliche Intelligenzen (KI) können Fehler machen. Wir arbeiten daran,
        dies so weit wie möglich zu minimieren, aber eine 100%ige Genauigkeit kann
        leider nicht garantiert werden.</p>
      <p>Um die Genauigkeit zu erhöhen, werden die Quellen angezeigt, auf denen
        die Antwort basiert. Die KI-Antwort sollte als Recherchehilfe betrachtet
        werden.</p>

      <h2>Welche Daten werden gespeichert und wo?</h2>
      <p>Suchanfragen und generierte Daten werden in Log-Files auf Servern in
        Deutschland gespeichert, um die Software zu verbessern und ihre Qualität
        zu analysieren.</p>
      <p>Wir nutzen ein Large Language Model des französischen Unternehmens
      "Mistral", um KI-Antworten zu generieren. Suchanfragen werden an die API
      von Mistral gesendet.</p>

      <h2>Kontakt</h2>
      <p>Fragen, Kommentare und Anregungen sind gerne per E-Mail an <a
        href="mailto:hendrik@oklabflensburg.de">hendrik@oklabflensburg.de</a>
        zugänglich.</p>
    </div>
  </Dialog>
</template>
