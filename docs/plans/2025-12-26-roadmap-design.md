# FFT Library Roadmap: Foundation-First Strategy

**High-performance, open-source Fast Fourier Transform (FFT) library for Java, designed to be the fastest and most reliable pure-Java option with verified benchmarks and comprehensive audio processing capabilities.**

**Date**: 2025-12-26
**Last Updated**: 2025-12-26
**Status**: Design Approved (PAL Reviewed)
**Approach**: Balanced (publish + optimizations + features)
**Timeline**: Flexible, milestone-driven

> **Note**: This is a living document and will be updated as the project progresses. See git history for version changes.

---

## Table of Contents

- [Executive Summary](#executive-summary)
- [Phase 0: Current State](#phase-0-current-state)
- [Non-Goals](#non-goals)
- [Vision and Strategy](#vision-and-strategy)
- [Phase 1: Technical Excellence](#phase-1-technical-excellence)
- [Phase 2: Public Launch](#phase-2-public-launch)
- [Phase 3: Ecosystem Growth](#phase-3-ecosystem-growth)
- [Definition of Done](#definition-of-done)
- [Risk Mitigation](#risk-mitigation)
- [Flexibility and Adaptation](#flexibility-and-adaptation)
- [Success Metrics Summary](#success-metrics-summary)
- [Immediate Next Steps](#immediate-next-steps)

---

## Executive Summary

Build the reference Java FFT library through verifiable technical excellence, then gradually grow the ecosystem and community. **Foundation-First** approach: publish only when performance numbers are solid and comparable.

**Strategic Sequence:**
1. **Fase 1**: Technical Excellence (polish + comparative benchmarks)
2. **Fase 2**: Public Launch (Maven Central + marketing)
3. **Fase 3**: Ecosystem Growth (features + community)

**Core Principles:**
- **Quality over Speed**: Milestone-driven, not time-driven
- **Transparency**: Honest benchmarks, even when not #1
- **Simplicity**: Conservative refactoring, maintainable codebase
- **Community-Ready**: From the start, even if publish is Phase 2

---

## Phase 0: Current State

### Baseline Assessment (Starting Point)

**Current Features:**
- ✅ Core FFT implementations: sizes 8-65536 (power-of-2)
- ✅ Twiddle factor cache: 30-50% universal speedup
- ✅ Pitch detection: Spectral FFT method (0.92% error) + YIN validation
- ✅ Song recognition: 47+ songs database (JSON + embedded)
- ✅ Real-time audio: Microphone capture, chord recognition
- ✅ Factory pattern: Auto-discovery via annotations

**Current Performance:**
- ✅ **FFT8**: 2.27x speedup vs FFTBase (verified with 10K warmup)
- ✅ **Twiddle cache**: 30-50% improvement across all sizes
- ✅ **FFT128**: 1.42x speedup (existing optimizations)
- ✅ **FFT16-512**: Neutral performance (overhead eliminated)
- ⚠️ **FFT16-32**: Not yet optimized beyond twiddle cache (opportunity)

**Current Testing:**
- ✅ 410 tests total (392 active, 7 disabled, 11 demos)
- ✅ Coverage: 90% line, 85% branch (JaCoCo enforced)
- ✅ CI/CD: GitHub Actions (build, test, coverage)
- ✅ Performance: JMH benchmarks with rigorous methodology

**Current Documentation:**
- ✅ 2500+ lines across multiple guides
- ✅ CLAUDE.md, USER_GUIDE.md, DOCUMENTATION_INDEX.md
- ✅ Performance optimization reports (FASE 1-2 complete)
- ✅ Lessons learned documented
- ✅ Clean git history (7 professional commits, 213 original preserved)

**Known Gaps:**
- ❌ Not published on Maven Central (zero external accessibility)
- ❌ No public comparative benchmarks (claims not externally verified)
- ❌ FFT16-32 not yet optimized beyond twiddle cache
- ❌ Limited audio features (no MFCC, noise reduction, beat detection)
- ❌ No external contributors yet (solo project)

**Starting Strengths:**
- Strong foundation with verified performance claims
- Comprehensive testing and documentation
- Clean, professional codebase ready for public release
- Unique features (song recognition, real-time audio)

---

## Non-Goals

### What This Library Is NOT

To manage expectations and prevent scope creep, explicitly **out of scope**:

❌ **Not a native performance replacement**
- Pure-Java focus, not competing with C/C++ FFTW raw speed
- Will never match JNI-wrapped native libraries on raw throughput
- Focus: Best *pure-Java* option, not fastest option period

❌ **Not a full DSP library**
- Focused on FFT and audio analysis, not comprehensive signal processing
- Won't implement: IIR filters, FIR filters, convolution (unless audio-specific)
- Recommendation: Use Apache Commons Math for general DSP

❌ **Not a GUI application**
- Library only, users build their own applications
- Demos provided for educational purposes, not production UIs
- Integration examples (JavaFX, Swing) but not full applications

❌ **Not targeting mobile initially**
- JVM/desktop focus (Windows, Linux, macOS)
- Android compatibility possible but not optimized for
- Phase 3+ consideration if there's demand

❌ **Not a real-time audio framework**
- Provides FFT primitives, not full audio routing/mixing
- Users integrate with existing frameworks (javax.sound, JACK, etc.)
- Focus: Fast FFT, not audio I/O infrastructure

❌ **Not backwards compatible with legacy APIs**
- Clean break from old FFTUtils patterns (already done)
- SemVer from 1.0.0 forward, no legacy baggage

**Why Non-Goals Matter:**
- Clarifies scope for contributors
- Prevents feature creep
- Helps users understand positioning vs alternatives
- Guides Phase 3 feature prioritization

---

## Vision and Strategy

### Overall Vision

Construct the reference Java FFT library through verifiable technical excellence, then gradually grow the ecosystem and community. Publish only when performance numbers are solid, impressive, and comparable with market leaders.

### Three-Phase Strategy

#### **Phase 1: Technical Excellence** 🏗️
- **Focus**: Code polish, comparative public benchmarking
- **Output**: Technical credibility, verifiable numbers, solid reputation
- **Motto**: "Publish only when you have something impressive to say"

#### **Phase 2: Public Launch** 🚀
- **Focus**: Maven Central, marketing-ready documentation, visibility
- **Output**: Accessibility, initial adoption, feedback loop
- **Motto**: "Make it easy to try the best Java FFT library"

#### **Phase 3: Ecosystem Growth** 🌱
- **Focus**: Advanced audio features, community, integrations
- **Output**: Real use cases, external contributors, market leadership
- **Motto**: "From library to audio processing platform"

### Guiding Principles

1. **Quality over Speed**: Milestone-driven, not time-driven
2. **Transparency**: Honest benchmarks, even when not #1 in everything
3. **Simplicity**: Conservative refactoring, maintainable codebase
4. **Community-Ready**: From day one, even though publish is Phase 2
5. **YAGNI Ruthlessly**: Remove unnecessary features from all designs

---

## Phase 1: Technical Excellence

### Objective

Build technical credibility through incremental polish and transparent comparative benchmarking. Phase complete when you have solid numbers to present publicly without embarrassment.

### Milestone 1.1: Conservative Refinement

**Estimated Effort**: 2-3 weeks

**Micro-Optimization Targets:**
- **Strategic inlining**: Hot-path methods <50 bytecode
- **Constant folding**: Pre-calculate common mathematical constants
- **Loop optimization**: Reduce bound checks where safe
- **Realistic target**: 1.1-1.3x incremental improvement on FFT16-128
- **Guarantee**: Zero regressions (complete test suite validation)

**Techniques:**
- Profile-guided optimization (identify hot spots)
- JIT-friendly code patterns (help compiler optimize)
- Cache-friendly memory access (reduce cache misses)
- Eliminate unnecessary object allocations

**Output:**
- Branch `performance-polish` with micro-optimizations
- JMH benchmarks before/after for each optimization
- Document: `OPTIMIZATION_REPORT.md` with methodology
- All changes merged to main with squash commit

**Success Criteria:**
- Measurable improvement (even if just 1.1x)
- Zero test failures
- Code remains readable and maintainable
- Optimizations documented with rationale

### Milestone 1.2: Comparative Benchmark Suite

**Estimated Effort**: 3-4 weeks

**Competitor Setup:**
- **JTransforms**: Current Java leader (most popular)
- **Apache Commons Math**: Standard reference (widely used)
- **FFTW-JNI**: Bridge to C native library (performance baseline)

**Metrics Compared:**
- **Throughput**: ops/second per size (8, 16, 32, 64, 128, 256, 512, 1024, 2048, 4096)
- **Latency**: p50, p95, p99 percentiles (critical for real-time audio)
- **Accuracy**: Error vs mathematical reference (correctness verification)
- **Memory**: Heap usage, GC pressure (resource efficiency)

**Infrastructure:**
- GitHub Actions workflow: regenerate benchmarks every release
- Data format: CSV + JSON for interactive charts
- Visualization: Chart.js on GitHub Pages
- Fairness guarantee: Same hardware, same JVM, consistent warmup

**Robustness Against System Noise:**
- **Extended warmup**: 10,000+ iterations (proven for FFT8)
- **Multiple forks**: 5+ separate processes to isolate JIT variance
- **Statistical rigor**: Median + confidence intervals (p5-p95)
- **Outlier detection**: Remove runs with >15% standard deviation
- **CI-specific config**: Dedicated runner without other workloads
- **Local reproduction**: Scripts with CPU idle >80% check
- **Disclaimer**: "Results from CI environment. Your mileage may vary."

**Output:**
- Separate repository `fft-benchmark` (transparency)
- GitHub Pages: `https://<username>.github.io/fft-benchmark`
- Interactive charts comparing all libraries
- Methodology document (reproducibility)
- README badge: "Benchmarked vs JTransforms"

**Honesty Policy:**
- Show all metrics, even where we lose
- Explain methodology transparently
- Update benchmarks with every release
- Invite scrutiny and corrections

---

## Phase 2: Public Launch

### Objective

Make the library easily accessible and discoverable when you have solid numbers to show. Maven Central publication with marketing based on verified performance.

### Milestone 2.1: Maven Central Publication

**Estimated Effort**: 1-2 weeks

**Infrastructure Setup:**
- **Sonatype OSSRH account**: Registration + domain validation
- **GPG signing**: Keys for artifact signing (Central requirement)
- **POM completion**: Developers, SCM, licenses metadata
- **Release automation**: GitHub Actions for automatic deployment
- **Versioning**: Strict SemVer (start with `1.0.0`)

**Artifact Structure:**
```
com.fft:fast-fourier-transform:1.0.0
├── fft-1.0.0.jar (main artifact)
├── fft-1.0.0-sources.jar (required by Central)
├── fft-1.0.0-javadoc.jar (required by Central)
└── fft-1.0.0.pom (metadata)
```

**Licensing and Legal:**
- ✅ LICENSE file in repository root (Apache 2.0)
- ✅ License headers on all source files (automated via Maven plugin)
- ✅ NOTICE file if required (list dependencies with license requirements)
- ✅ POM metadata complete (developers, SCM, licenses)

**Quality Checks Before Publish:**
- ✅ All tests passing (410 tests)
- ✅ Coverage >90% line, >85% branch (JaCoCo)
- ✅ No SNAPSHOT dependencies
- ✅ Javadoc builds without warnings
- ✅ Dependency versions up-to-date (or documented if using older)
- ✅ Dependabot or Renovate enabled (automatic dependency updates)

**Versioning Strategy:**
- **Major (X.0.0)**: Breaking API changes, incompatible modifications
- **Minor (1.X.0)**: New features, backward-compatible additions (Phase 3 features)
- **Patch (1.0.X)**: Bug fixes, performance improvements, documentation updates
- **When to increment**: After each milestone completion with user-facing changes

**Implementation Notes:**
- Claude has full autonomy for implementation
- Follow latest best practices (2025 standards)
- Use GitHub Actions for CI/CD automation
- GPG key management via GitHub Secrets
- Automated release on git tag push
- Start Sonatype OSSRH registration IMMEDIATELY (parallel to Phase 1)

**Output:**
- Library searchable on Maven Central
- `mvn dependency:get -Dartifact=com.fft:fast-fourier-transform:1.0.0` works
- Sonatype sync completed (<4 hours)
- GitHub Release with changelog
- Download badge in README

### Milestone 2.2: Marketing-Ready Documentation

**Estimated Effort**: 1-2 weeks

**README Enhancement:**
- **Above the fold**: Benchmark results with chart
- **Quick Start**: 3-step copy/paste for Maven/Gradle
- **USP clear**: "Fastest pure-Java FFT with verified benchmarks"
- **Badges**: Maven Central version, CI status, coverage, benchmarks

**New Documentation:**
- **BENCHMARKS.md**: Complete methodology, link to comparisons
- **QUICKSTART.md**: 5-minute tutorial for first FFT
- **MIGRATION.md**: From JTransforms/Commons Math (attract competitor users)

**Marketing Channels:**
- Reddit post: r/java (respectful, factual)
- HackerNews: "Show HN: Fast Java FFT library"
- Dev.to article: Technical deep-dive
- Twitter/LinkedIn: Brief announcement

**Feedback Mechanisms:**
- **GitHub Discussions** enabled for Q&A, feature requests, general discussion
- **Issue templates**: Bug report, feature request, question
- **Optional user survey** after 3-6 months post-publish
  - "What are you using it for?"
  - "What features are missing?"
  - "Performance satisfaction rating?"
  - Tool: Google Forms, Typeform, or GitHub Discussions poll

**Output:**
- Professional README that sells the library
- Complete getting-started experience
- Migration path for existing users
- Initial visibility on social channels
- Feedback loop established for Phase 3 prioritization

---

## Phase 3: Ecosystem Growth

### Objective

Expand from FFT library to audio processing platform, attract contributors, create real use cases. Begins after initial traction from Maven Central.

### Milestone 3.1: Advanced Audio Features

**Estimated Effort**: Variable, feature-driven

**Feature Tier 1 (Quick Wins):**
- **Spectrogram Generation**: Time-frequency visualization (already have FFT)
- **Windowing Functions**: Hamming, Hanning, Blackman (beyond basic)
- **Frequency Band Analysis**: Extract bass/mid/treble energy
- **Zero-crossing rate**: Simple feature extraction

**Feature Tier 2 (Differentiators):**
- **Mel-frequency Cepstral Coefficients (MFCC)**: Speech/music analysis standard
- **Noise Reduction**: Spectral subtraction, Wiener filtering
- **Audio Fingerprinting**: Shazam-like (extend song recognition)
- **Beat Detection**: Tempo and rhythm analysis

**Feature Tier 3 (Ambitious):**
- **Speech Recognition basics**: Keyword spotting
- **Music Transcription**: Note detection → MIDI export
- **Real-time Effects**: Vocoder, auto-tune, pitch shift

**Priority Strategy**:
- Complete ALL Tier 1 features first
- Tier 2: Selective, based on user requests/issues
- Tier 3: Only if there's demonstrated demand

**Implementation Approach:**
- One feature at a time (no parallel work)
- Each feature: implementation → tests → docs → release
- Minor version bump per feature (1.1.0, 1.2.0, etc.)
- Examples/demos for each feature

### Milestone 3.2: Community & Ecosystem

**Ongoing Effort**

**Contributor Experience:**
- **CONTRIBUTING.md**: Clear, welcoming, concrete examples
- **Good First Issues**: Labeled and curated (e.g., "Add Blackman window")
- **Feature Template**: Issue template for new feature proposals
- **Code Review**: Automation (lint, tests) + constructive feedback
- **Response time**: Reply to issues/PRs within 48 hours

**Ecosystem Integrations:**
- **Spring Boot Starter**: Auto-configuration for audio processing apps
- **Audio File I/O**: Wrapper for javax.sound, support WAV/MP3
- **Visualization**: Integration with JavaFX/Swing for spectrograms
- **Examples Gallery**: Real-world use cases (tuner app, visualizer, etc.)

**Success Metrics:**
- 5-10 external contributors within 12 months post-publish
- 3-5 public projects using the library
- Stack Overflow tag `fast-fourier-transform-java`
- 100+ downloads/month from Maven Central

**Community Philosophy:**
- Welcome all skill levels
- Celebrate contributions (shout-outs in release notes)
- Quick feedback (even if it's "will review this weekend")
- Constructive code review (suggest improvements, don't just reject)

---

## Definition of Done

### How to Avoid "Might Never Finish"

Every milestone has **objective** completion criteria. When all criteria are ✅, move to next milestone without hesitation.

### Phase 1 - Done When:

#### Milestone 1.1 (Polish):
- ✅ At least 3 **measurable and documented** micro-optimizations implemented
  - Each with JMH before/after benchmark showing improvement (even if small)
  - Documented rationale in `OPTIMIZATION_REPORT.md`
- ✅ JMH benchmark shows 1.1-1.3x on FFT16-128 (median, 5 forks, 10K+ warmup)
- ✅ Zero regressions: test suite 100% passing
- ✅ `OPTIMIZATION_REPORT.md` published and committed
- ✅ Branch merged to main with squash commit

**Fallback:** If after 3 attempts don't reach 1.1x, move to 1.2 (twiddle cache already provides value)

#### Milestone 1.2 (Benchmarks):
- ✅ 3 competitor libraries integrated and working
- ✅ 4 metrics (throughput, latency, accuracy, memory) automated
- ✅ GitHub Pages live with interactive charts
- ✅ CI workflow generates benchmarks on:
  - Every push to `main` branch
  - Every tagged release
  - Manual trigger (workflow_dispatch)
  - Weekly scheduled run (optional)
- ✅ README badge "Benchmarks" linked

**Fallback:** If benchmark setup too complex (>4 weeks), publish minimal "vs JTransforms" only

**Phase 1 Complete:** Both milestones 1.1 and 1.2 done → **STOP** and move to Phase 2.

**Time-box Override:** If 3 calendar months pass without Phase 1 completion, trigger emergency publish with MVP (see Risk Mitigation).

### Phase 2 - Done When:

#### Milestone 2.1 (Maven Central):
- ✅ Artifact published on Maven Central (searchable)
- ✅ `mvn dependency:get -Dartifact=com.fft:fast-fourier-transform:1.0.0` works
- ✅ Sonatype sync completed (<4 hours)
- ✅ GitHub Release created with changelog
- ✅ Release automation tested (1 dry-run + 1 real release)

#### Milestone 2.2 (Docs):
- ✅ README has working 3-step Quick Start (validated internally)
- ✅ Benchmark badge and chart above-the-fold
- ✅ `BENCHMARKS.md`, `QUICKSTART.md`, `MIGRATION.md` exist
- ✅ Javadoc published and linked from README
- ✅ GitHub Discussions enabled
- ✅ At least 1 external person **invited** to beta test quick start
  - If unavailable before publish, make it Phase 2 success metric instead

**Phase 2 Complete:** Both milestones done → **CELEBRATE** 🎉 → Move to Phase 3.

**Celebration:** Write blog post summarizing launch, post on social media, treat yourself!

### Phase 3 - Iterative (no final "done"):

#### For Each Feature (Tier 1/2/3):
- ✅ Complete implementation with tests
- ✅ Javadoc and example in README/docs
- ✅ Release minor version (1.X.0)
- ✅ Announcement on social/blog (optional)

#### Success Signals Phase 3:
- 🎯 10+ GitHub stars within 3 months post-publish
- 🎯 1+ issue/PR from external contributor
- 🎯 100+ downloads/month from Maven Central

---

## Risk Mitigation

### Risks Per Phase and Countermeasures

### Phase 1: Technical Excellence

**Risk 1.1**: *Optimizations don't yield measurable benefits*
- **Mitigation**: If after 3 attempts you don't reach 1.1x, STOP and move to 1.2
- **Fallback**: Twiddle cache already provides 30-50%, can publish without additional polish

**Risk 1.2**: *Benchmarks unfair or controversial*
- **Mitigation**: Publish methodology BEFORE results, ask for review
- **Transparency**: If you lose in some metric, show it honestly
- **Narrative**: "Best at X, competitive at Y, working on Z"

**Risk 1.3**: *Benchmark setup too complex, gets stuck*
- **Mitigation**: Time-box to 4 weeks effort. If incomplete, publish "Benchmark vs JTransforms" (just 1 competitor)
- **MVP**: Even just throughput on 5 sizes is better than nothing

**Risk 1.4 (Low Probability, High Impact)**: *Benchmarks show library is uncompetitive across the board*
- **Mitigation (If happens)**:
  1. **Deep Dive**: Profile and analyze *why* - fundamental flaw or benchmark setup issue?
  2. **Re-evaluate Strategy**: If truly uncompetitive, pivot to different USP:
     - **Ease of Use**: Simplest API, best documentation, most examples
     - **Specific Niche**: Best for specific use case (real-time audio, embedded)
     - **Feature Richness**: More audio processing features than competitors
     - **Modern Codebase**: Cleanest code, best tests, most maintainable
  3. **Honest Communication**: If performance isn't the win, don't claim it is - focus on actual strengths
  4. **Acceptance**: It's okay to not be the fastest if you offer other value
- **Likelihood**: Very low (twiddle cache proven 30-50%, FFT8 verified 2.27x)
- **Impact**: High (would require major strategy pivot)

### Phase 2: Public Launch

**Risk 2.1**: *Sonatype OSSRH request rejected or very slow*
- **Mitigation**: Start request IMMEDIATELY (parallel to Phase 1), can take 1-2 weeks
- **Fallback**: Publish on GitHub Packages if Sonatype doesn't work

**Risk 2.2**: *Zero downloads/interest post-publish*
- **Mitigation**: Proactive marketing: Reddit (r/java), HackerNews, Dev.to posts
- **Acceptance**: Even 10 downloads/month first month is OK, takes time
- **Community**: ALWAYS respond to issues/questions within 48h (show you're alive)

### Phase 3: Ecosystem Growth

**Risk 3.1**: *Feature creep - implement too much without priority*
- **Mitigation**: One Tier 1 feature at a time. Tier 2 only if there's demand (issue requests)
- **YAGNI ruthless**: If nobody asks for a feature, don't implement it

**Risk 3.2**: *No external contributors ever*
- **Acceptance**: OK! Many successful libraries are one-person shows
- **Focus shift**: If after 6 months zero PRs, focus on features instead of community

### Meta Risk: "Might Never Finish" 🌊

**Structural Countermeasures:**

1. **Rigid Definition of Done**: When milestone is ✅, you MUST move to next
2. **Emergency time-box**: If Phase 1 > 3 months calendar time, publish anyway (even without perfect benchmarks)
3. **Accountability**: Public roadmap commit on GitHub (positive social pressure)
4. **Minimum Viable Phases**: Every phase has MVP version if you get stuck:
   - Phase 1 MVP: Only polish OR only benchmarks (not both)
   - Phase 2 MVP: Only Maven Central (minimal docs)
   - Phase 3 MVP: Even just 1 Tier 1 feature is progress

**Escalation Triggers:**
- If 6 months pass and still in Phase 1 → FORCE publish (go to Phase 2 MVP)
- If 12 months pass and haven't published → Re-evaluate strategy with fresh brainstorming

### Additional Meta Risks

**Risk: Burnout / Loss of Motivation**
- **Scenario**: Solo side project, 5-15 hours/week sustainable *in theory*, but life happens
- **Mitigation**:
  1. **It's okay to pause**: This is a side project, not a job. Life > code.
  2. **Communicate pause publicly**: If break >1 month, post brief issue/discussion: "Taking a break, back around [date/month]"
  3. **Document state before pausing**: Commit current work, update roadmap with "Paused as of [date]" note
  4. **Low-maintenance mode**: Post-Phase 2 publish, maintenance (bug fixes, dependency updates) is lower effort than features
- **Acceptance**: A stable library at Phase 2 completion (Maven Central, docs, benchmarks) is still valuable, even if Phase 3 incomplete
- **Bus Factor**: Document everything clearly (already doing), clear licensing allows forks if necessary

**Risk: Technology Shift / Java Ecosystem Changes (Long-term)**
- **Scenario**: Java evolves (new JVM features), or FFT landscape changes (faster library emerges, hardware-accelerated becomes standard)
- **Mitigation**: Monitor ecosystem, be ready to adapt
  - **New JVM features**: Evaluate Project Valhalla (value types), Project Loom, SIMD APIs
  - **Competitor shifts**: If dramatically faster library emerges, re-assess USP (ease-of-use, features, specific niche)
  - **Hardware trends**: If GPU/FPGA FFT becomes ubiquitous in Java, consider integration or acknowledge pure-CPU niche
- **Action**: Annual landscape review, no immediate changes needed

---

## Flexibility and Adaptation

**This is a design, not a contract.**

Expect to:
- Adjust priorities based on user feedback
- Skip features with no demand
- Accelerate milestones that go faster than expected
- Time-box things that get stuck
- Pivot strategy if market changes

**Review Points:**
- After Phase 1: Assess if benchmarks changed strategy
- After Phase 2: First 30 days post-publish, analyze adoption
- After each Phase 3 feature: Was it worth it? Keep going or pivot?

**Document Updates:**
- Update this roadmap when pivoting (commit changes)
- Keep old versions in git history (learn from changes)
- Annual review: Is this still the right direction?

---

## Success Metrics Summary

### Phase 1 Success:
- ✅ Optimization report published
- ✅ Comparative benchmarks live
- ✅ Honest performance claims verified

### Phase 2 Success:
- ✅ Maven Central searchable
- ✅ 10+ downloads first month
- ✅ 1+ GitHub star from external user

### Phase 3 Success (12 months post-publish):
- 🎯 100+ downloads/month
- 🎯 10+ GitHub stars
- 🎯 3+ Tier 1 features shipped
- 🎯 1+ external contributor

### Ultimate Success (Long-term):
- 🏆 Referenced as "best pure-Java FFT" in blog posts/papers
- 🏆 Used in 10+ public projects
- 🏆 Active community (regular issues/PRs)
- 🏆 Competitive or leading performance vs alternatives

---

## Immediate Next Steps

**Post-Roadmap Approval Checklist:**

- ✅ Review roadmap with PAL MCP (completed via Gemini Flash)
- ✅ Incorporate PAL feedback (Phase 0, Non-Goals, refined DoD, risks)
- ⬜ Commit roadmap to git (`docs/plans/2025-12-26-roadmap-design.md`)
- ⬜ (Optional) Announce roadmap publicly (GitHub Discussions, blog) - "Here's the plan!"
- ⬜ **Choose**: Start Milestone 1.1 (Polish) or 1.2 (Benchmarks)?
  - **Option A**: Start 1.1 - Create `performance-polish` branch, identify first micro-optimization target
  - **Option B**: Start 1.2 - Begin Sonatype OSSRH registration in parallel (long lead time)
  - **Option C**: Do both in parallel (polish code while waiting for OSSRH approval)

**Recommended First Action:**
1. Start Sonatype OSSRH registration TODAY (can take 1-2 weeks approval)
2. While waiting, begin Milestone 1.1 (Polish) - quick wins build momentum
3. Parallel track maximizes time efficiency

---

**Living Document**: This roadmap will be updated as the project progresses. See git history for version changes and evolution of strategy.

**Links to Supporting Documents:**
- [PERFORMANCE_OPTIMIZATION_STATUS.md](../../PERFORMANCE_OPTIMIZATION_STATUS.md) - History of optimization work
- [docs/testing/JMH_BENCHMARKING_GUIDE.md](../testing/JMH_BENCHMARKING_GUIDE.md) - Detailed benchmarking methodology
- [USER_GUIDE.md](../../USER_GUIDE.md) - Current user-facing documentation
- [DOCUMENTATION_INDEX.md](../../DOCUMENTATION_INDEX.md) - Master navigation to all docs

---

**Roadmap Version**: 1.0 (PAL Reviewed)
**Next Review**: After Phase 1 completion or 3 months, whichever comes first
