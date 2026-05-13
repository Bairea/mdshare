# 每日总结

今天完成了 Markdown 渲染方案评估。

| 方案 | 成本 | 适用场景 | 风险 |
| --- | --- | --- | --- |
| 本地渲染 | 低 | MVP | 复杂表格适配 |
| 云端渲染 | 中 | 一致性强 | 网络依赖 |

```text
Next step:
- preview
- export
- share
```


```Python
import time
import cv2
import numpy as np
from auto_flip_ana import needs_horizontal_flip_v2

DATA_IMG_PATH = "/pipeline/proj/visium_hd_compatibility/auto_flip_ana/data/test_shape_qc.png"
HE_IMG_PATH = "/pipeline/proj/visium_hd_compatibility/auto_flip_ana/data/GKT-T1-260511.tif"
VIZ_OUTPUT_PATH = "/pipeline/proj/visium_hd_compatibility/auto_flip_ana/output/flip_comparison.png"
CSV_OUTPUT_PATH = "/pipeline/proj/visium_hd_compatibility/auto_flip_ana/output/config_metrics.csv"


def load_data_image(path: str) -> np.ndarray:
    img = cv2.imread(path, cv2.IMREAD_UNCHANGED)
    if img is None:
        raise FileNotFoundError(f"Cannot read {path}")
    print(f"  Data image raw: shape={img.shape}, dtype={img.dtype}")

    if img.ndim == 3:
        if img.shape[2] == 4:
            img = cv2.cvtColor(img[:, :, :3], cv2.COLOR_RGB2GRAY)
        elif img.shape[2] == 3:
            img = cv2.cvtColor(img, cv2.COLOR_RGB2GRAY)
        else:
            img = img[:, :, 0]

    print(f"  Data image processed: shape={img.shape}, dtype={img.dtype}")
    return img
```
