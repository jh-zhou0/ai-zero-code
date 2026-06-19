/**
 * 可视化编辑模式相关常量与脚本
 * 包含注入到 iframe 中的 CSS 样式和 JavaScript 脚本
 */

/** 注入到 iframe 的 CSS：hover 和 selected 效果 */
export const VISUAL_EDIT_CSS = `
.__ve-hover {
  outline: 2px solid #1890ff !important;
  outline-offset: -2px !important;
  cursor: pointer !important;
}
.__ve-selected {
  outline: 3px solid #096dd9 !important;
  outline-offset: -2px !important;
  background-color: rgba(24, 144, 255, 0.06) !important;
}
`

/**
 * 注入到 iframe 中执行的脚本（IIFE 自执行）
 * - 监听 mouseover/mouseout 实现 hover 边框
 * - 监听 click 选中元素并通过 postMessage 通知父窗口
 * - 监听 message 事件处理清除选中等指令
 */
export const VISUAL_EDIT_SCRIPT = `;(function() {
  var SELECTED_CLASS = '__ve-selected';
  var selectedEl = null;

  function getSelector(el) {
    if (el.id && document.getElementById(el.id) === el) return '#' + el.id;
    var path = [];
    while (el && el.nodeType === Node.ELEMENT_NODE) {
      var selector = el.tagName.toLowerCase();
      if (el.id) {
        path.unshift('#' + el.id);
        break;
      }
      var parent = el.parentElement;
      if (parent) {
        var children = Array.from(parent.children);
        var index = children.indexOf(el) + 1;
        if (children.length > 1) {
          selector += ':nth-child(' + index + ')';
        }
      }
      path.unshift(selector);
      el = parent;
    }
    return path.join(' > ');
  }

  function getElementInfo(el) {
    var cls = '';
    if (typeof el.className === 'string') {
      cls = el.className;
    } else if (el.className && el.className.baseVal) {
      cls = el.className.baseVal;
    }
    var text = (el.textContent || '').trim().substring(0, 100);
    return {
      tag: el.tagName.toLowerCase(),
      id: el.id || undefined,
      className: cls || undefined,
      text: text || undefined,
      selector: getSelector(el)
    };
  }

  document.addEventListener('mouseover', function(e) {
    e.target.classList.add('__ve-hover');
  }, true);

  document.addEventListener('mouseout', function(e) {
    e.target.classList.remove('__ve-hover');
  }, true);

  document.addEventListener('click', function(e) {
    e.preventDefault();
    e.stopPropagation();
    if (selectedEl) {
      selectedEl.classList.remove(SELECTED_CLASS);
    }
    selectedEl = e.target;
    selectedEl.classList.add(SELECTED_CLASS);
    window.parent.postMessage({ type: 'visual-edit-element-selected', element: getElementInfo(e.target) }, '*');
  }, true);

  window.addEventListener('message', function(event) {
    if (event.data.type === 'visual-edit-clear') {
      if (selectedEl) {
        selectedEl.classList.remove(SELECTED_CLASS);
        selectedEl = null;
      }
    }
  });
})();`
