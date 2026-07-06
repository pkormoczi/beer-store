#!/usr/bin/env python3
"""Render a Trivy JSON scan report as a standalone styled HTML page."""
import json
import sys
from html import escape

SEVERITY_ORDER = ["CRITICAL", "HIGH", "MEDIUM", "LOW", "UNKNOWN"]
SEVERITY_COLORS = {
    "CRITICAL": "#dc2626",
    "HIGH": "#ea580c",
    "MEDIUM": "#ca8a04",
    "LOW": "#65a30d",
    "UNKNOWN": "#6b7280",
}


def severity_rank(severity):
    return SEVERITY_ORDER.index(severity) if severity in SEVERITY_ORDER else len(SEVERITY_ORDER)


def render_row(vuln):
    severity = vuln.get("Severity", "UNKNOWN")
    color = SEVERITY_COLORS.get(severity, "#6b7280")
    url = vuln.get("PrimaryURL", "") or "#"
    return f"""
        <tr>
          <td><span class="badge" style="background:{color}">{escape(severity)}</span></td>
          <td>{escape(vuln.get("PkgName", ""))}</td>
          <td>{escape(vuln.get("InstalledVersion", ""))}</td>
          <td>{escape(vuln.get("FixedVersion", "") or "-")}</td>
          <td><a href="{escape(url)}" target="_blank" rel="noopener">{escape(vuln.get("VulnerabilityID", ""))}</a></td>
          <td>{escape(vuln.get("Title", "") or "")}</td>
        </tr>"""


def main():
    json_path, html_path = sys.argv[1], sys.argv[2]

    with open(json_path, encoding="utf-8") as f:
        data = json.load(f)

    vulnerabilities = [
        vuln
        for result in (data.get("Results") or [])
        for vuln in (result.get("Vulnerabilities") or [])
    ]
    vulnerabilities.sort(key=lambda v: severity_rank(v.get("Severity", "UNKNOWN")))

    counts = {}
    for vuln in vulnerabilities:
        severity = vuln.get("Severity", "UNKNOWN")
        counts[severity] = counts.get(severity, 0) + 1

    if counts:
        summary = "".join(
            f'<span class="badge" style="background:{SEVERITY_COLORS.get(sev, "#6b7280")}">{sev}: {count}</span>'
            for sev, count in sorted(counts.items(), key=lambda kv: severity_rank(kv[0]))
        )
    else:
        summary = '<span class="badge" style="background:#16a34a">No vulnerabilities found</span>'

    rows = "".join(render_row(v) for v in vulnerabilities)
    if not rows:
        rows = '<tr><td colspan="6">No vulnerabilities found.</td></tr>'

    html = f"""<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="utf-8">
<title>Trivy scan report</title>
<style>
  body {{ font-family: -apple-system, "Segoe UI", Roboto, sans-serif; margin: 2rem; background: #f8fafc; color: #1e293b; }}
  h1 {{ font-size: 1.5rem; }}
  .badge {{ display: inline-block; padding: 0.25rem 0.6rem; border-radius: 999px; color: white; font-size: 0.8rem; font-weight: 600; margin: 0 0.5rem 0.5rem 0; }}
  table {{ width: 100%; border-collapse: collapse; margin-top: 1.5rem; background: white; box-shadow: 0 1px 3px rgba(0,0,0,0.1); border-radius: 8px; overflow: hidden; }}
  th, td {{ padding: 0.6rem 0.8rem; text-align: left; border-bottom: 1px solid #e2e8f0; font-size: 0.9rem; vertical-align: top; }}
  th {{ background: #f1f5f9; font-weight: 600; }}
  a {{ color: #2563eb; text-decoration: none; }}
  a:hover {{ text-decoration: underline; }}
  .back {{ margin-bottom: 1rem; display: inline-block; color: #2563eb; }}
</style>
</head>
<body>
  <a class="back" href="../">&larr; Back</a>
  <h1>Trivy container image scan</h1>
  <p>{summary}</p>
  <table>
    <thead><tr><th>Severity</th><th>Package</th><th>Installed</th><th>Fixed</th><th>CVE</th><th>Title</th></tr></thead>
    <tbody>{rows}</tbody>
  </table>
</body>
</html>
"""

    with open(html_path, "w", encoding="utf-8") as f:
        f.write(html)


if __name__ == "__main__":
    main()
