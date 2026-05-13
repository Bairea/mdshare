window.__MD_SHARE_RENDER_READY__ = false;
window.__MD_SHARE_EXPORT_MODE__ = false;

function applyRenderScale(scale) {
  document.documentElement.style.setProperty("--render-scale", String(scale));
}

function updateRenderScale() {
  var stage = document.querySelector(".render-stage");
  var card = document.querySelector(".render-card");
  if (!stage || !card) {
    return;
  }

  if (window.__MD_SHARE_EXPORT_MODE__) {
    applyRenderScale(1);
    return;
  }

  var targetWidth = card.offsetWidth || 1080;
  var availableWidth = stage.clientWidth || targetWidth;
  var scale = Math.min(1, availableWidth / targetWidth);
  applyRenderScale(scale);
}

window.__MD_SHARE_SET_EXPORT_MODE__ = function (enabled) {
  window.__MD_SHARE_EXPORT_MODE__ = enabled === true;
  document.documentElement.classList.toggle("md-share-export-mode", window.__MD_SHARE_EXPORT_MODE__);
  updateRenderScale();
  return window.__MD_SHARE_EXPORT_MODE__ ? "export" : "preview";
};

document.addEventListener("DOMContentLoaded", function () {
  document.querySelectorAll("pre code").forEach(function (block) {
    if (window.hljs && typeof window.hljs.highlightElement === "function") {
      window.hljs.highlightElement(block);
    } else {
      block.classList.add("hljs");
    }
  });

  document.querySelectorAll("table").forEach(function (table) {
    var columnCount = table.querySelectorAll("tr:first-child th, tr:first-child td").length;
    if (columnCount >= 5) {
      table.classList.add("compact-table");
    }
  });

  updateRenderScale();
  setTimeout(function () {
    updateRenderScale();
    window.__MD_SHARE_RENDER_READY__ = true;
  }, 0);
});

window.addEventListener("resize", updateRenderScale);
