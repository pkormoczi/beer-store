---
name: update-docs
description: This skill should be used when the user asks to "update the docs",
  "sync the docs", "refresh CLAUDE.md", "update the README", "refresh the architecture
  diagrams", "keep the markdown fresh", or in Hungarian "frissítsd a doksit / dokumentációt",
  or right after code changes that may have made README.md, docs/architektura-diagramok.md,
  or CLAUDE.md stale. Reconciles those three markdown files against the current source of
  truth (the src/main/java tree, package-info.java allowedDependencies, the POMs,
  application.yml/config, and .github/workflows/ci.yml), edits only what drifted, and
  reports a summary without committing.
version: 0.1.0
---

# Update docs

Reconcile `README.md`, `docs/architektura-diagramok.md`, and `CLAUDE.md` (repo root) against
the current source of truth, edit only what has drifted, and report a summary. Never commit —
leave `git status`/`git diff` for the user to review and commit themselves.

## Scope

Exactly three files: `README.md`, `docs/architektura-diagramok.md`, `CLAUDE.md`. Nothing else
gets rewritten. `docs/ADR.MD` and `docs/TESTING_STRATEGY.md` are out of scope for content edits —
only check that every `ADR-NN` tag referenced from the three files resolves to a real entry in
`docs/ADR.MD` (see Step 3).

Keep every edit minimal: change only the drifted fact (a class name, a table cell, a checkbox,
a version, a diagram label). Do not rewrite prose style, reorder sections, change emoji, or
"improve" wording that wasn't wrong. Preserve each file's existing language — Hungarian for
`docs/architektura-diagramok.md`, the current language of `README.md`/`CLAUDE.md`.

## Step 1 — determine what to check

Default to a **diff-driven** pass, scoped to what actually changed:

1. For each of the three files, find when it was last touched: `git log -1 --format=%H -- <file>`.
2. Diff the source tree since each of those commits, plus any uncommitted working-tree changes:
   `git diff <hash>..HEAD -- <paths>` and `git status --porcelain -- <paths>`, where `<paths>` is
   the relevant source surface: `beer-store-application/src/main/java/**`,
   `**/package-info.java`, `pom.xml` files (root + all modules), `application.yml`, `config/`,
   `.github/workflows/ci.yml`.
3. Only re-verify the sections of each doc whose source paths actually show changes. Skip
   sections whose source is untouched — don't re-derive facts that haven't moved.

Run a **full audit** instead — walking every checklist item in `references/drift-map.md` — when
the user explicitly asks for "full"/"teljes" reconciliation, or when step 1–2 can't establish a
clear starting point (e.g. the docs have no prior commit, or the diff is too broad to scope).

## Step 2 — the two coupled blocks

These two loops must move together. If one side changes, check the other sides before finishing
— a partial update leaves the docs internally inconsistent, which is worse than leaving them
untouched:

1. **`CLAUDE.md` "Module reference" table ⟷ `docs/architektura-diagramok.md` diagrams 2 and 4 ⟷
   the `order`/`customer`/`product` source trees.** A class rename, move, or new
   port/adapter in any of these three modules must be reflected in all three places. See
   `references/drift-map.md` for exactly which classes each diagram hard-codes.
2. **`README.md` endpoint/command lists ⟷ `CLAUDE.md` endpoint/command lists.** Both files list
   the same Swagger/SOAP URLs, the same contract-first build order, and the same
   Postgres/`ddl-auto: validate` story — duplicated content that must stay word-for-word
   consistent (or intentionally reference each other rather than duplicate, if that's cleaner).

## Step 3 — cross-reference and Mermaid checks

Run these regardless of diff scope — they're cheap and catch silent breakage:

- **ADR references:** every `ADR-NN` tag appearing in `CLAUDE.md` or
  `docs/architektura-diagramok.md` must resolve to a real, matching entry in `docs/ADR.MD`. Flag
  (don't guess-fix) any tag that doesn't resolve — that's a decision for the user, not something
  to invent.
- **Mermaid syntax** in `docs/architektura-diagramok.md` (lessons learned from prior breakage in
  this exact file — verify both hold after any edit to a diagram):
  - No generic-type angle brackets (`List<BeerView>`) inside `sequenceDiagram` message text — the
    parser chokes on it. Use plain words instead (e.g. "BeerView lista").
  - A labeled dotted edge in a `flowchart` must use `A -.->|label text| B`, never the
    `A -. label text .-> B` sandwich form — the latter breaks when the label contains a hyphen
    (which Hungarian compound words often do).
- **Don't invent features.** A `[ ]` → `[x]` flip in `README.md`'s checklist (or vice versa) must
  be backed by an actual code/config fact — e.g. don't tick a box because it looks plausible.
  Where a checklist item names a property (`spring.threads.virtual.enabled`,
  `spring.mvc.problemdetails.enabled`), confirm the exact property name and value still match
  `application.yml`.
- **Don't touch intentional conventions.** Things like the Liquibase single-folder policy
  (`CLAUDE.md` "Testing conventions") are deliberate project rules, not facts that drift — leave
  prose describing a *policy* alone; only fix prose describing a *fact* (a class name, a table
  cell, a version pin) that the source no longer supports.

For the full inventory of which section of which file mirrors which source path, see
`references/drift-map.md` — load it before a full audit, or when a diff-driven pass needs to
know a section's exact source-of-truth.

## Step 4 — edit

Make the edits directly in the three files. Keep each edit surgical (see Scope above). If a fact
can't be confidently resolved from the source (e.g. an aspirational `[ ]` item with no clear
code signal either way), leave it as-is and call it out in the summary rather than guessing.

## Step 5 — report, don't commit

After editing, report a summary table: **file → what changed → why / based on which source.**
Include anything flagged but left unresolved (broken ADR ref, ambiguous checklist item). Then
stop — do not stage or commit. The user reviews `git status`/`git diff` and commits themselves
(this project's convention: commit directly to master, small separate commits, no branch/PR
ceremony — but that's the user's call to make, not this skill's).

If nothing drifted, say so plainly — don't manufacture a change to have something to report.
