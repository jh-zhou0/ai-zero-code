/**
 * useVisualEdit - 可视化编辑 composable
 * 管理编辑模式状态、与 iframe 的通信、选中元素处理
 */
import { ref } from 'vue'
import { VISUAL_EDIT_CSS, VISUAL_EDIT_SCRIPT } from '@/utils/visualEdit'

export interface SelectedElement {
  tag: string
  id?: string
  className?: string
  text?: string
  selector: string
}

export function useVisualEdit() {
  const isEditMode = ref(false)
  const selectedElement = ref<SelectedElement | null>(null)

  let injectedIframe: HTMLIFrameElement | null = null

  /**
   * 向 iframe 中注入可视化编辑所需 CSS 和 JS
   */
  function injectIntoIframe(iframe: HTMLIFrameElement): boolean {
    try {
      const doc = iframe.contentDocument || iframe.contentWindow?.document
      if (!doc) return false

      // 注入 CSS
      let styleEl = doc.getElementById('__visual_edit_style__') as HTMLStyleElement | null
      if (!styleEl) {
        styleEl = doc.createElement('style')
        styleEl.id = '__visual_edit_style__'
        styleEl.textContent = VISUAL_EDIT_CSS
        doc.head.appendChild(styleEl)
      }

      // 注入 JS
      let scriptEl = doc.getElementById('__visual_edit_script__') as HTMLScriptElement | null
      if (!scriptEl) {
        scriptEl = doc.createElement('script')
        scriptEl.id = '__visual_edit_script__'
        scriptEl.textContent = VISUAL_EDIT_SCRIPT
        doc.body.appendChild(scriptEl)
      }

      injectedIframe = iframe
      return true
    } catch {
      return false
    }
  }

  /**
   * 从 iframe 中移除可视化编辑的 CSS 和 JS
   */
  function removeFromIframe(iframe: HTMLIFrameElement) {
    try {
      const doc = iframe.contentDocument || iframe.contentWindow?.document
      if (!doc) return
      doc.getElementById('__visual_edit_style__')?.remove()
      doc.getElementById('__visual_edit_script__')?.remove()
    } catch {
      // 跨域或文档不可访问时静默忽略
    }
  }

  /**
   * 进入编辑模式
   */
  function enterEditMode(iframe: HTMLIFrameElement | null) {
    isEditMode.value = true
    selectedElement.value = null

    if (!iframe) return

    // 尝试立即注入
    const success = injectIntoIframe(iframe)
    if (!success) {
      // iframe 未就绪，等 load 事件后再注入
      const onLoad = () => {
        injectIntoIframe(iframe)
        iframe.removeEventListener('load', onLoad)
      }
      iframe.addEventListener('load', onLoad)
    }
  }

  /**
   * 退出编辑模式
   */
  function exitEditMode() {
    isEditMode.value = false
    selectedElement.value = null

    if (injectedIframe) {
      // 通知 iframe 清除选中状态
      try {
        injectedIframe.contentWindow?.postMessage({ type: 'visual-edit-clear' }, '*')
      } catch {
        // 忽略
      }
      removeFromIframe(injectedIframe)
      injectedIframe = null
    }
  }

  /**
   * 移除选中的元素（不清除注入脚本）
   */
  function removeSelectedElement() {
    selectedElement.value = null
    if (injectedIframe) {
      try {
        injectedIframe.contentWindow?.postMessage({ type: 'visual-edit-clear' }, '*')
      } catch {
        // 忽略
      }
    }
  }

  /**
   * 生成要追加到提示词末尾的元素信息文本
   */
  function getElementPromptSuffix(): string {
    if (!selectedElement.value) return ''
    const info = selectedElement.value
    let desc = `\n\n[用户选中的页面元素]`
    desc += `\n- 标签: ${info.tag}`
    if (info.id) desc += `\n- id: ${info.id}`
    if (info.className) desc += `\n- class: ${info.className}`
    if (info.text) desc += `\n- 文本内容: "${info.text}"`
    desc += `\n- CSS选择器: ${info.selector}`
    desc += `\n请根据以上选中的元素进行修改`
    return desc
  }

  // ---- 消息监听 ----

  function handleMessage(event: MessageEvent) {
    if (event.data?.type === 'visual-edit-element-selected') {
      selectedElement.value = event.data.element as SelectedElement
    }
  }

  function setupMessageListener() {
    window.addEventListener('message', handleMessage)
  }

  function teardownMessageListener() {
    window.removeEventListener('message', handleMessage)
  }

  return {
    isEditMode,
    selectedElement,
    enterEditMode,
    exitEditMode,
    removeSelectedElement,
    getElementPromptSuffix,
    injectIntoIframe,
    setupMessageListener,
    teardownMessageListener,
  }
}
