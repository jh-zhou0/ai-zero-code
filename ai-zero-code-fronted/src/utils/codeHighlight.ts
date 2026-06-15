/**
 * Markdown 代码高亮配置
 * 为 highlight.js 注册更多常用文件类型的语言别名支持
 */
import hljs from 'highlight.js'
import 'highlight.js/styles/github.css'

// 按类别导入需要的语言模块
import xml from 'highlight.js/lib/languages/xml'
import css from 'highlight.js/lib/languages/css'
import javascript from 'highlight.js/lib/languages/javascript'
import typescript from 'highlight.js/lib/languages/typescript'
import bash from 'highlight.js/lib/languages/bash'
import json from 'highlight.js/lib/languages/json'
import yaml from 'highlight.js/lib/languages/yaml'
import markdown from 'highlight.js/lib/languages/markdown'
import python from 'highlight.js/lib/languages/python'
import java from 'highlight.js/lib/languages/java'
import php from 'highlight.js/lib/languages/php'
import sql from 'highlight.js/lib/languages/sql'
import dockerfile from 'highlight.js/lib/languages/dockerfile'
import nginx from 'highlight.js/lib/languages/nginx'
import cpp from 'highlight.js/lib/languages/cpp'
import csharp from 'highlight.js/lib/languages/csharp'
import go from 'highlight.js/lib/languages/go'
import rust from 'highlight.js/lib/languages/rust'
import ruby from 'highlight.js/lib/languages/ruby'
import less from 'highlight.js/lib/languages/less'
import scss from 'highlight.js/lib/languages/scss'
import objectivec from 'highlight.js/lib/languages/objectivec'
import swift from 'highlight.js/lib/languages/swift'
import kotlin from 'highlight.js/lib/languages/kotlin'
import scala from 'highlight.js/lib/languages/scala'
import perl from 'highlight.js/lib/languages/perl'
import r from 'highlight.js/lib/languages/r'
import lua from 'highlight.js/lib/languages/lua'
import haskell from 'highlight.js/lib/languages/haskell'
import erlang from 'highlight.js/lib/languages/erlang'
import elixir from 'highlight.js/lib/languages/elixir'
import vbnet from 'highlight.js/lib/languages/vbnet'
import powershell from 'highlight.js/lib/languages/powershell'
import protobuf from 'highlight.js/lib/languages/protobuf'
import plaintext from 'highlight.js/lib/languages/plaintext'
import ini from 'highlight.js/lib/languages/ini'
import stylus from 'highlight.js/lib/languages/stylus'
import groovy from 'highlight.js/lib/languages/groovy'
import makefile from 'highlight.js/lib/languages/makefile'

/** highlight.js registerLanguage 的第二参数类型 */
type LanguageFn = Parameters<typeof hljs.registerLanguage>[1]

interface LanguageRegistration {
  name: string
  fn: LanguageFn
}

// 注册语言模块
const registrations: LanguageRegistration[] = [
  { name: 'xml', fn: xml },
  { name: 'css', fn: css },
  { name: 'javascript', fn: javascript },
  { name: 'typescript', fn: typescript },
  { name: 'bash', fn: bash },
  { name: 'json', fn: json },
  { name: 'yaml', fn: yaml },
  { name: 'markdown', fn: markdown },
  { name: 'python', fn: python },
  { name: 'java', fn: java },
  { name: 'php', fn: php },
  { name: 'sql', fn: sql },
  { name: 'dockerfile', fn: dockerfile },
  { name: 'nginx', fn: nginx },
  { name: 'cpp', fn: cpp },
  { name: 'csharp', fn: csharp },
  { name: 'go', fn: go },
  { name: 'rust', fn: rust },
  { name: 'ruby', fn: ruby },
  { name: 'less', fn: less },
  { name: 'scss', fn: scss },
  { name: 'objectivec', fn: objectivec },
  { name: 'swift', fn: swift },
  { name: 'kotlin', fn: kotlin },
  { name: 'scala', fn: scala },
  { name: 'perl', fn: perl },
  { name: 'r', fn: r },
  { name: 'lua', fn: lua },
  { name: 'haskell', fn: haskell },
  { name: 'erlang', fn: erlang },
  { name: 'elixir', fn: elixir },
  { name: 'vbnet', fn: vbnet },
  { name: 'powershell', fn: powershell },
  { name: 'protobuf', fn: protobuf },
  { name: 'plaintext', fn: plaintext },
  { name: 'ini', fn: ini },
  { name: 'stylus', fn: stylus },
  { name: 'groovy', fn: groovy },
  { name: 'makefile', fn: makefile },
]

for (const { name, fn } of registrations) {
  hljs.registerLanguage(name, fn)
}

/**
 * 常用文件类型的语言别名映射表
 * key: markdown 代码块中的 lang 标识（可能是文件扩展名或别名）
 * value: highlight.js 中注册的对应语言名称
 */
const languageAliases: Record<string, string> = {
  // 前端模板/框架
  'vue': 'xml',
  'vue-html': 'xml',
  'svelte': 'xml',
  'jsx': 'javascript',
  'tsx': 'typescript',
  // shell 变体
  'shell': 'bash',
  'zsh': 'bash',
  'sh': 'bash',
  'powershell': 'powershell',
  'ps1': 'powershell',
  // yaml 变体
  'yml': 'yaml',
  // markdown 变体
  'md': 'markdown',
  // docker 变体
  'docker': 'dockerfile',
  'containerfile': 'dockerfile',
  'docker-compose': 'yaml',
  // nginx
  'conf': 'nginx',
  // 纯文本
  'text': 'plaintext',
  'txt': 'plaintext',
  // 后端语言变体
  'c++': 'cpp',
  'c': 'cpp',
  'c#': 'csharp',
  'cs': 'csharp',
  'node': 'javascript',
  'js': 'javascript',
  'ts': 'typescript',
  'py': 'python',
  'rb': 'ruby',
  'rs': 'rust',
  'kt': 'kotlin',
  'sc': 'scala',
  'pl': 'perl',
  'hs': 'haskell',
  'erl': 'erlang',
  'ex': 'elixir',
  'exs': 'elixir',
  'vb': 'vbnet',
  'proto': 'protobuf',
  'gradle': 'groovy',
  'cmake': 'cmake',
  'makefile': 'makefile',
  // 样式类
  'sass': 'scss',
  'postcss': 'css',
  'stylus': 'stylus',
  'styl': 'stylus',
  // 数据库
  'mysql': 'sql',
  'postgresql': 'sql',
  'pgsql': 'sql',
  'psql': 'sql',
  'sqlite': 'sql',
  'plsql': 'sql',
  // 配置类
  'toml': 'ini',
  'ini': 'ini',
  'cfg': 'ini',
  'env': 'bash',
}

/**
 * 解析高亮语言标识
 * 支持各种文件类型变体名称
 */
function resolveLanguage(lang: string): string {
  const lower = lang.toLowerCase()
  // 先从别名映射中查找
  if (languageAliases[lower]) {
    return languageAliases[lower]
  }
  // 如果 highlight.js 自身支持该语言名，直接使用
  if (hljs.getLanguage(lower)) {
    return lower
  }
  // 如果 highlight.js 支持原名称（未转小写），使用原名称
  if (hljs.getLanguage(lang)) {
    return lang
  }
  return ''
}

/**
 * HTML 转义（用 charCode 方式避免格式化工具替换实体字符）
 */
function escapeHtml(text: string): string {
  let result = ''
  for (let i = 0; i < text.length; i++) {
    const c = text.charCodeAt(i)
    if (c === 38) { result += '&' + 'amp;' }
    else if (c === 60) { result += '&' + 'lt;' }
    else if (c === 62) { result += '&' + 'gt;' }
    else if (c === 34) { result += '&' + 'quot;' }
    else { result += text[i] }
  }
  return result
}

/**
 * 代码高亮渲染函数
 * 供 markdown-it 的 highlight 回调使用
 */
export function highlightCode(str: string, lang: string): string {
  const resolvedLang = resolveLanguage(lang)
  if (resolvedLang && hljs.getLanguage(resolvedLang)) {
    try {
      const highlighted = hljs.highlight(str, {
        language: resolvedLang,
        ignoreIllegals: true,
      }).value
      return `<pre class="hljs"><code class="language-${resolvedLang}">${highlighted}</code></pre>`
    } catch {
      // fallback 到纯文本
    }
  }
  // 无语言或高亮失败时，转义 HTML 后原样输出
  const escaped = escapeHtml(str)
  return `<pre class="hljs"><code>${escaped}</code></pre>`
}
