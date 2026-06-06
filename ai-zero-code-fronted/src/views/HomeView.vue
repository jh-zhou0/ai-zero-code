<script setup lang="ts">
import { check } from '@/api/healthController.ts'
import { ref } from 'vue'
const open = ref<boolean>(false)

const showModal = () => {
  open.value = true;
}

const handleOk = (e: MouseEvent) => {
  console.log(e);
  open.value = false;
}

check().then((res) => {
  console.log(res)
})

</script>

<template>
  <main>
    <div>
      <a-button type="primary" @click="showModal">Open Modal</a-button>
      <a-modal v-model:open="open" title="Basic Modal" @ok="handleOk">
        <p>Some contents...</p>
        <p>Some contents...</p>
        <p>Some contents...</p>
      </a-modal>
    </div>
  </main>
</template>
