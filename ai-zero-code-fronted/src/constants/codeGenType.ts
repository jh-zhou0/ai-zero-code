/**
 * 代码生成类型枚举
 * 对应后端 CodeGenTypeEnum
 */
export const CodeGenTypeEnum = {
  HTML: {
    value: 'html',
    label: '原生 HTML 模式',
  },
  MULTI_FILE: {
    value: 'multi_file',
    label: '原生多文件模式',
  },
  VUE_PROJECT: {
    value: 'vue_project',
    label: 'Vue 工程模式'
  },
} as const

export type CodeGenTypeValue = (typeof CodeGenTypeEnum)[keyof typeof CodeGenTypeEnum]['value']

/**
 * 根据 value 获取 label
 */
export function getCodeGenTypeLabel(value?: string): string {
  if (!value) return '-'
  for (const key of Object.keys(CodeGenTypeEnum)) {
    const item = CodeGenTypeEnum[key as keyof typeof CodeGenTypeEnum]
    if (item.value === value) {
      return item.label
    }
  }
  return value
}
