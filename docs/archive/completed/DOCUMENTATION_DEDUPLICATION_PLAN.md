# Documentation Deduplication Plan

**Created**: October 6, 2025
**Purpose**: Eliminate duplicate and obsolete documentation

---

## Analysis Summary

**Total Markdown Files**: 27
**Duplicates Identified**: 15 documents with overlapping content
**Obsolete Documents**: 11 historical documents
**Action**: Consolidate to 12 authoritative documents, archive 15

---

## Major Duplications Found

### 1. FASE 2 Performance Documents (4 overlapping documents)

| Document | Lines | Date | Status | Content |
|----------|-------|------|--------|---------|
| FASE2_FINAL_REPORT.md | 457 | Oct 6 | Keep | Comprehensive FASE 2 report |
| FASE2_OVERHEAD_REMOVAL.md | 143 | Oct 6 | **Archive** | Subset of final report |
| OPTIMIZATION_LESSONS_LEARNED.md | 268 | Oct 6 | **Archive** | Overlaps with lessons learned |
| FASE_2_LESSONS_LEARNED.md | 258 | Oct 6 | **Keep** | Concise lessons, good format |

**Consolidation Strategy**:
- **Primary**: Keep FASE_2_LESSONS_LEARNED.md (best format, concise)
- **Secondary**: Keep FASE2_FINAL_REPORT.md (comprehensive reference)
- **Archive**: FASE2_OVERHEAD_REMOVAL.md, OPTIMIZATION_LESSONS_LEARNED.md

**Overlap**: 80% content duplication (FFT8 success, delegation overhead, lessons learned)

---

### 2. Performance Planning Documents (obsolete vs current)

| Document | Lines | Date | Status | Issue |
|----------|-------|------|--------|-------|
| PERFORMANCE_IMPROVEMENT_PLAN.md | 171 | Aug 30 | **Archive** | Talks about problems now resolved |
| PERFORMANCE_OPTIMIZATION_STATUS.md | 261 | Current | **Keep** | Current status |

**Issue with PERFORMANCE_IMPROVEMENT_PLAN.md**:
- Discusses FFT32, FFT2048, FFT4096 correctness issues that were resolved in FASE 1
- "Status: Analysis Complete, Implementation Pending" - now completed
- Outdated 3-phase plan superseded by actual FASE 1/2 work

**Action**: Archive PERFORMANCE_IMPROVEMENT_PLAN.md

---

### 3. Refactoring Documents (roadmap vs summary)

| Document | Lines | Status | Purpose |
|----------|-------|--------|---------|
| REFACTORING_ROADMAP.md | 732 | **Archive** | Detailed 6-phase plan (historical) |
| REFACTORING_SUMMARY.md | 172 | **Keep** | Concise summary of completed work |

**Reason**: ROADMAP is 732 lines of detailed planning, mostly completed. SUMMARY covers achievements concisely.

---

### 4. Status Documents (outdated)

| Document | Lines | Date | Status |
|----------|-------|------|--------|
| CURRENT_STATUS_SUMMARY.md | 124 | June 5 | **Archive** |
| PERFORMANCE_OPTIMIZATION_STATUS.md | 261 | Current | **Keep** |

**Reason**: CURRENT_STATUS_SUMMARY is outdated (June vs October), superseded by PERFORMANCE_OPTIMIZATION_STATUS.

---

### 5. Historical Cleanup Documents (all completed work)

| Document | Lines | Purpose | Status |
|----------|-------|---------|--------|
| CLEANUP_PLAN.md | 106 | Cleanup planning | **Archive** (completed) |
| CLEANUP_SUMMARY.md | 187 | Cleanup results | **Archive** (completed) |
| FIXES_APPLIED.md | 188 | Applied fixes log | **Archive** (completed) |
| CRITICAL_ISSUES.md | 119 | Issue tracking | **Archive** (all resolved) |
| CORRECTED_ANALYSIS.md | 127 | Analysis corrections | **Archive** (historical) |
| OPTIMIZATION_ACTIONS.md | 48 | Action items | **Archive** (completed) |

**Total**: 875 lines of historical documentation, all work completed.

---

### 6. Other Documents to Evaluate

| Document | Lines | Action | Reason |
|----------|-------|--------|--------|
| CONSENSUS_ANALYSIS.md | 284 | **Keep** | Multi-agent FFT8 variance analysis (useful) |
| AGENTS.md | 31 | **Archive** | Brief coordination notes (not needed) |
| PERFORMANCE_MEASUREMENT_CRISIS.md | 279 | **Keep** | Important investigation, lessons learned |

---

## Consolidation Actions

### Step 1: Archive Historical Documents (11 files)

Move to `docs/archive/historical/`:
1. CLEANUP_PLAN.md
2. CLEANUP_SUMMARY.md
3. FIXES_APPLIED.md
4. CRITICAL_ISSUES.md
5. CORRECTED_ANALYSIS.md
6. OPTIMIZATION_ACTIONS.md
7. CURRENT_STATUS_SUMMARY.md
8. AGENTS.md
9. REFACTORING_ROADMAP.md (keep reference copy)
10. PERFORMANCE_IMPROVEMENT_PLAN.md
11. FASE2_OVERHEAD_REMOVAL.md

### Step 2: Archive Duplicate FASE 2 Documents (1 file)

Move to `docs/archive/fase2/`:
1. OPTIMIZATION_LESSONS_LEARNED.md (keep FASE_2_LESSONS_LEARNED.md instead)

### Step 3: Create Archive Structure

```
docs/
├── archive/
│   ├── historical/          # Completed work, old status
│   │   ├── CLEANUP_PLAN.md
│   │   ├── CLEANUP_SUMMARY.md
│   │   ├── FIXES_APPLIED.md
│   │   ├── CRITICAL_ISSUES.md
│   │   ├── CORRECTED_ANALYSIS.md
│   │   ├── OPTIMIZATION_ACTIONS.md
│   │   ├── CURRENT_STATUS_SUMMARY.md
│   │   ├── AGENTS.md
│   │   ├── REFACTORING_ROADMAP.md
│   │   └── PERFORMANCE_IMPROVEMENT_PLAN.md
│   └── fase2/               # FASE 2 duplicate documents
│       ├── OPTIMIZATION_LESSONS_LEARNED.md
│       └── FASE2_OVERHEAD_REMOVAL.md
└── DEMO_DOCUMENTATION.md    # Move from docs/ to root
```

### Step 4: Keep as Primary Documentation (12 files)

**Active Documentation** (root directory):
1. README.md - User documentation
2. CLAUDE.md - Development guide
3. DOCUMENTATION_INDEX.md - Master index (update needed)

**Performance & Optimization** (root directory):
4. PERFORMANCE_OPTIMIZATION_STATUS.md - Current status
5. PROFILING_RESULTS.md - Profiling data
6. FASE_2_LESSONS_LEARNED.md - FASE 2 lessons
7. FASE2_FINAL_REPORT.md - FASE 2 comprehensive report
8. PERFORMANCE_MEASUREMENT_CRISIS.md - Investigation lessons
9. CONSENSUS_ANALYSIS.md - Multi-agent variance analysis

**Testing & Architecture** (root directory):
10. VALIDATION_FRAMEWORK.md - Testing approach
11. JMH_BENCHMARKING_GUIDE.md - Benchmarking guide
12. REFACTORING_SUMMARY.md - Architecture summary
13. TESTING_COMPLIANCE.md - Test compliance

**Implementation Reports** (root directory):
14. P0_IMPLEMENTATION_SUMMARY.md - P0 work summary
15. P1_IMPLEMENTATION_SUMMARY.md - P1 work summary

**Demo Documentation** (docs/ directory):
16. docs/DEMO_DOCUMENTATION.md
17. docs/DEMO_TESTING_SUMMARY.md
18. docs/FFT_Library.md

---

## Content Consolidation

### Update PERFORMANCE_OPTIMIZATION_STATUS.md

Add missing content from archived documents:
- Summary of resolved issues from PERFORMANCE_IMPROVEMENT_PLAN.md
- Historical context about correctness fixes
- Link to archived documents for full history

### Update FASE_2_LESSONS_LEARNED.md

Ensure it includes:
- FFT8 manual optimization details (from OPTIMIZATION_LESSONS_LEARNED.md)
- Delegation overhead analysis (from FASE2_OVERHEAD_REMOVAL.md)
- All key insights from both documents

### Update DOCUMENTATION_INDEX.md

- Remove archived documents from main listing
- Add "Archive" section pointing to docs/archive/
- Update file counts and statistics
- Add archival policy

---

## Expected Results

### Before
- 27 markdown files (root + docs/)
- 15 documents with duplicated content
- 11 obsolete historical documents
- Confusing navigation
- 6,524 total lines of markdown

### After
- 15 active markdown files
- 12 archived documents in organized structure
- Zero content duplication
- Clear documentation hierarchy
- ~4,000 lines of active documentation

### Benefits
1. **Reduced Confusion**: Single source of truth for each topic
2. **Better Navigation**: Clear categorization
3. **Preserved History**: Archives available for reference
4. **Easier Maintenance**: No duplicate updates needed
5. **Improved Discoverability**: Logical organization

---

## Implementation Checklist

- [ ] Create docs/archive/historical/ directory
- [ ] Create docs/archive/fase2/ directory
- [ ] Move 11 historical documents to archive
- [ ] Move 2 FASE 2 duplicates to archive
- [ ] Update PERFORMANCE_OPTIMIZATION_STATUS.md with consolidated content
- [ ] Update FASE_2_LESSONS_LEARNED.md with consolidated content
- [ ] Update DOCUMENTATION_INDEX.md with new structure
- [ ] Create docs/archive/README.md explaining archive
- [ ] Test all links in remaining documents
- [ ] Commit changes with descriptive message

---

## Archive Policy

**Criteria for Archival**:
1. Work completed and documented elsewhere
2. Superseded by more recent/comprehensive document
3. Historical planning documents for completed work
4. Duplicate content available in authoritative source

**Archive Access**:
- All archived documents remain in git history
- docs/archive/ provides organized access
- DOCUMENTATION_INDEX.md links to important archives
- Archive README explains what each document was for

---

## Validation

After consolidation, verify:
1. All current information available in primary docs
2. No broken links in active documentation
3. Archive structure documented in index
4. Git history preserves all content
5. README and CLAUDE.md remain accurate

---

**Status**: Plan complete, ready for implementation
