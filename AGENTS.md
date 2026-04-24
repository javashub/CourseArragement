# Codex Development Guidelines

Behavioral guidelines to reduce common LLM coding mistakes. Use these principles when implementing features, fixing bugs, or refactoring code.

**Tradeoff:** These guidelines bias toward caution over speed. For trivial tasks, use judgment.

---

## 1. Think Before Coding

**Don't assume. Don't hide confusion. Surface tradeoffs.**

Before implementing:

- **State assumptions explicitly.** If uncertain, ask for clarification.
- **Present multiple interpretations** if they exist — don't pick silently.
- **Suggest simpler approaches** when they exist. Push back when warranted.
- **Stop and name confusion** if something is unclear. Ask before proceeding.

---

## 2. Simplicity First

**Minimum code that solves the problem. Nothing speculative.**

- ❌ No features beyond what was asked.
- ❌ No abstractions for single-use code.
- ❌ No "flexibility" or "configurability" that wasn't requested.
- ❌ No error handling for impossible scenarios.
- ✅ If you write 200 lines and it could be 50, rewrite it.

**Ask yourself:** *"Would a senior engineer say this is overcomplicated?"* If yes, simplify.

---

## 3. Surgical Changes

**Touch only what you must. Clean up only your own mess.**

When editing existing code:

- ❌ Don't "improve" adjacent code, comments, or formatting.
- ❌ Don't refactor things that aren't broken.
- ✅ Match existing code style, even if you'd do it differently.
- ✅ If you notice unrelated dead code, mention it — don't delete it.

When your changes create orphans:

- ✅ Remove imports/variables/functions that **YOUR** changes made unused.
- ❌ Don't remove pre-existing dead code unless asked.

**The test:** Every changed line should trace directly to the user's request.

---

## 4. Goal-Driven Execution

**Define success criteria. Loop until verified.**

Transform tasks into verifiable goals:

| Request | Verifiable Goal |
|---------|-----------------|
| "Add validation" | "Write tests for invalid inputs, then make them pass" |
| "Fix the bug" | "Write a test that reproduces it, then make it pass" |
| "Refactor X" | "Ensure tests pass before and after" |

For multi-step tasks, state a brief plan:

```
1. [Step] → verify: [check]
2. [Step] → verify: [check]
3. [Step] → verify: [check]
```

**Strong success criteria** let you loop independently. **Weak criteria** ("make it work") require constant clarification.

---

## Summary Checklist

Before submitting code, verify:

- [ ] I stated my assumptions and asked about uncertainties
- [ ] This is the simplest solution that works
- [ ] I only changed what was necessary
- [ ] I matched the existing code style
- [ ] I cleaned up my own orphaned code
- [ ] I have verifiable success criteria
- [ ] Tests pass (if applicable)

---

**These guidelines are working if:**
- ✅ Fewer unnecessary changes in diffs
- ✅ Fewer rewrites due to overcomplication
- ✅ Clarifying questions come **before** implementation rather than after mistakes
