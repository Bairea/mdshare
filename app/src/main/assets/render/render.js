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

  var vpBefore = "";
  var vpAfter = "";
  var viewportMeta = document.querySelector('meta[name="viewport"]');
  if (viewportMeta) {
    vpBefore = viewportMeta.getAttribute("content") || "";
    if (window.__MD_SHARE_EXPORT_MODE__) {
      var card = document.querySelector(".render-card");
      var canvasWidth = card ? card.offsetWidth : 1080;
      viewportMeta.setAttribute("content", "width=" + canvasWidth + ", initial-scale=1.0");
    } else {
      viewportMeta.setAttribute("content", "width=device-width, initial-scale=1.0");
    }
    vpAfter = viewportMeta.getAttribute("content") || "";
  }

  updateRenderScale();
  var result = window.__MD_SHARE_EXPORT_MODE__ ? "export" : "preview";
  console.log("[MdShare] SET_EXPORT_MODE(" + enabled + ") -> " + result +
    " | vp: '" + vpBefore + "' -> '" + vpAfter +
    "' | dpr=" + window.devicePixelRatio +
    " | render-scale=" + getComputedStyle(document.documentElement).getPropertyValue("--render-scale"));
  return result;
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
