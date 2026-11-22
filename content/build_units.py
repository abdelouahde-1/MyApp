"""Builds app/src/main/assets/units.json from the unit files in this folder.

Each uNN.py defines UNIT = unit(number, title, vocab=section(...), grammar=..., functions=...).
Question helpers: write the correct answer FIRST in mc(); the app shuffles the options.
  E/M/H  = difficulty
  mc(level, prompt, [correct, wrong, wrong...], why="", img=None)
  tf(level, prompt, True/False, why="")
  wr(level, prompt, ["accepted", "alternative"], why="", img=None)   # student types the answer
Run:  python3 content/build_units.py
"""
import importlib, json, os, sys

E, M, H = "EASY", "MEDIUM", "HARD"
_counter = {}


def _id(prefix):
    _counter[prefix] = _counter.get(prefix, 0) + 1
    return f"{prefix}-{_counter[prefix]}"


def mc(level, prompt, options, why="", img=None):
    assert len(options) >= 2 and len(set(options)) == len(options), prompt
    return dict(type="MULTIPLE_CHOICE", difficulty=level, prompt=prompt, options=options, correctIndex=0, explanation=why, image=img)


def tf(level, prompt, answer, why="", img=None):
    return dict(type="TRUE_FALSE", difficulty=level, prompt=prompt, correctIndex=0 if answer else 1, explanation=why, image=img)


def _expand(a):
    """'[word] ' marks an optional word: 'he said [that] he was' -> both variants."""
    import re
    m = re.search(r"\[([^\]]+)\] ?", a)
    if not m:
        return [a]
    with_w = a[:m.start()] + m.group(1) + " " + a[m.end():]
    without = a[:m.start()] + a[m.end():]
    return _expand(with_w) + _expand(without)


def wr(level, prompt, answers, why="", img=None):
    assert answers, prompt
    answers = [x for a in answers for x in _expand(a)]
    return dict(type="FILL_BLANK", difficulty=level, prompt=prompt, acceptedAnswers=answers, explanation=why, image=img)


def section(skill, title, content, questions):
    return dict(skill=skill, title=title, content=content.strip("\n"), questions=questions)


def unit(number, title, vocab, grammar, functions):
    uid = f"unit-{number:02d}"
    secs = [section("VOCABULARY", *vocab), section("GRAMMAR", *grammar), section("FUNCTIONS", *functions)]
    for s in secs:
        for q in s["questions"]:
            q["id"] = _id(f"{uid}-{s['skill'][0].lower()}")
            if q.get("image"):
                q["prompt"] = q["prompt"].replace(" " + q["image"], "").replace(q["image"], "").strip()
            if q.get("image") is None:
                q.pop("image", None)
    return dict(id=uid, number=number, title=title, sections=secs)


if __name__ == "__main__":
    here = os.path.dirname(os.path.abspath(__file__))
    sys.path.insert(0, here)
    units = []
    for f in sorted(os.listdir(here)):
        if f.startswith("u") and f.endswith(".py") and f[1:3].isdigit():
            units.append(importlib.import_module(f[:-3]).UNIT)
    out = os.path.join(here, "..", "app", "src", "main", "assets", "units.json")
    with open(out, "w", encoding="utf-8") as fh:
        json.dump(dict(units=units), fh, ensure_ascii=False, indent=1)
    for u in units:
        counts = " | ".join(
            f"{s['skill'][:4]} " + "/".join(str(sum(q['difficulty'] == d for q in s['questions'])) for d in (E, M, H))
            for s in u["sections"])
        print(f"Unit {u['number']:2d} {u['title'][:28]:28s} {counts}")
    print("total questions:", sum(len(s["questions"]) for u in units for s in u["sections"]))
