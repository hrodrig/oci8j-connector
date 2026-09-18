<!--
 * oci8j-connector - Oracle 8i REST API
 * Copyright (c) 2024 - 2026 Hermes Rodríguez
 * SPDX-License-Identifier: MIT
 -->

# Copyright Header Guide

How to add copyright headers to source files in **oci8j-connector**.

## Standard Header Format

For Java and other `/* */` source files:

```java
/*
 * oci8j-connector - Oracle 8i REST API
 * Copyright (c) 2024 - 2026 Hermes Rodríguez
 *
 * SPDX-License-Identifier: MIT
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 */
```

Short form (acceptable when the full MIT text lives in root `LICENSE`):

```java
/*
 * oci8j-connector - Oracle 8i REST API
 * Copyright (c) 2024 - 2026 Hermes Rodríguez
 * SPDX-License-Identifier: MIT
 */
```

Shell scripts (`#` comments):

```bash
#!/usr/bin/env bash
#
# oci8j-connector - Oracle 8i REST API
# Copyright (c) 2024 - 2026 Hermes Rodríguez
# SPDX-License-Identifier: MIT
#
```

Windows batch (`REM`):

```bat
@echo off
REM oci8j-connector - Oracle 8i REST API
REM Copyright (c) 2024 - 2026 Hermes Rodríguez
REM SPDX-License-Identifier: MIT
```

Canonical template: [`COPYRIGHT_HEADER.txt`](COPYRIGHT_HEADER.txt). Full license text: [`LICENSE`](LICENSE).

## Project license

This project is licensed under the **MIT License**.

### Oracle JDBC driver note

`lib/classes12.jar` is an **Oracle proprietary** JDBC driver. It is **not** covered by the MIT license of this repository. Redistribution and use of that JAR are governed by Oracle’s terms. Keep it as a system-scoped dependency; do not relicense it as MIT or GPL.

## FAQ

### Q: Why MIT and not GPL?

**A:** MIT matches the committed project license. Spring Boot (Apache-2.0) is compatible with MIT. GPL is not required by Java and conflicts awkwardly with the proprietary Oracle driver.

### Q: Must every file have the full MIT text?

**A:** No. SPDX + copyright line is enough if root `LICENSE` is present. Prefer the short form for scripts; full form for major entry points if desired.

## References

- [MIT License](https://opensource.org/licenses/MIT)
- [SPDX MIT](https://spdx.org/licenses/MIT.html)
