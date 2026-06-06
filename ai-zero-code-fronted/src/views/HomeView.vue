<script setup lang="ts">
import { check } from '@/api/healthController.ts'

check().then((res) => {
  console.log(res)
})

</script>

<template>
  <main>
  </main>
</template>
