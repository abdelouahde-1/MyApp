from build_units import *

VOCAB = """
# Sustainable development
**Sustainable development** means meeting the needs of the present without harming the ability of future generations to meet their own needs. It moves us from problems to solutions, from negative to positive.
It works on three issues: **social**, **economic** and **environmental**.

# Social & economic issues (problem → solution)
• **poverty** → job opportunities, fair distribution of wealth
• **unemployment** → **job opportunities**, investment, microcredits
• **discrimination** → **social justice**, equality
• **poor healthcare** → **quality healthcare**
• **illiteracy / poor education** → **good education**

# Environmental issues (problem → solution)
• **pollution** → **recycling**, clean energy
• **global warming / climate change** → **renewable energies**: **solar energy** (sun), **wind energy** (wind), hydro energy (water)
• **deforestation** (cutting down forests) → **reforestation** (planting trees)
• **drought** (lack of rain) → saving water

# Useful words
• **to preserve / protect** the environment · **resources** · **to reduce** waste · **greenhouse gases** · **fossil fuels** (oil, coal, gas)
"""

V = [
    mc(E, "Which energy comes from the sun?", ["solar energy", "wind energy", "fossil fuel", "nuclear waste"], img="☀️"),
    mc(E, "Which energy comes from the wind?", ["wind energy", "solar energy", "coal", "petrol"], img="🌬️"),
    mc(E, "Cutting down forests is called…", ["deforestation", "reforestation", "recycling", "drought"], img="🪓"),
    mc(E, "Planting new trees is called…", ["reforestation", "deforestation", "pollution", "poverty"], img="🌱"),
    mc(E, "Using old materials to make new products is called…", ["recycling", "polluting", "unemployment", "deforestation"], img="♻️"),
    mc(E, "Not having a job is called…", ["unemployment", "employment", "discrimination", "healthcare"], img="💼"),
    mc(E, "Being very poor is called…", ["poverty", "wealth", "justice", "recycling"]),
    mc(E, "The rise in the Earth's temperature is…", ["global warming", "reforestation", "social justice", "recycling"], img="🌡️"),
    mc(E, "Treating people unfairly because of their race, gender or religion is…", ["discrimination", "social justice", "equality", "healthcare"]),
    mc(E, "A long period without rain is a…", ["drought", "flood", "storm", "harvest"], img="🏜️"),
    tf(E, "Sustainable development works on three issues: social, economic and environmental.", True),
    tf(E, "Pollution is a solution to global warming.", False, "Pollution is a problem."),

    mc(M, "Which is a SOLUTION to unemployment?", ["job opportunities", "discrimination", "poverty", "deforestation"]),
    mc(M, "Which is a SOLUTION to discrimination?", ["social justice", "poor healthcare", "pollution", "unemployment"]),
    mc(M, "Which is a SOLUTION to poor healthcare?", ["quality healthcare", "global warming", "poverty", "drought"]),
    mc(M, "Solar and wind energy are both ___ energies.", ["renewable", "fossil", "polluting", "expensive"]),
    mc(M, "Which is an ENVIRONMENTAL problem?", ["climate change", "unemployment", "discrimination", "poverty"]),
    mc(M, "Which is a SOCIAL problem?", ["poverty", "deforestation", "global warming", "pollution"]),
    mc(M, "We should ___ our nature because we can't live without it.", ["preserve", "pollute", "destroy", "waste"]),
    mc(M, "Recent ___ in Morocco is due to the lack of rain.", ["drought", "flood", "reforestation", "recycling"]),
    wr(M, "The opposite of deforestation is ___.", ["reforestation", "afforestation"]),
    wr(M, "Energy that never runs out (sun, wind, water) is called ___ energy.", ["renewable", "green", "clean"]),
    wr(M, "The opposite of “unemployment” is ___.", ["employment"]),

    wr(H, "Give the noun of “poor”.", ["poverty"]),
    wr(H, "Give the noun of “pollute”.", ["pollution"]),
    wr(H, "Give the verb of “recycling”.", ["recycle"]),
    wr(H, "Give the adjective of “sustainability” (as in “___ development”).", ["sustainable"]),
    wr(H, "Give the noun of “discriminate”.", ["discrimination"]),
    mc(H, "“Development that meets the needs of the present without harming the needs of future generations” is…", ["sustainable development", "industrial development", "global warming", "economic crisis"]),
    mc(H, "Which pair is a correct problem → solution?", ["deforestation → reforestation", "poverty → unemployment", "pollution → drought", "social justice → discrimination"]),
    mc(H, "Which word is the odd one out?", ["unemployment", "solar energy", "wind energy", "recycling"], "The others are solutions; unemployment is a problem."),
    mc(H, "Burning oil, coal and gas (fossil fuels) produces greenhouse gases, which cause…", ["global warming", "reforestation", "social justice", "literacy"]),
    tf(H, "“Renewable energies” are energies that can run out, like oil and coal.", False, "Renewable energies never run out (sun, wind…); oil and coal are fossil fuels."),
]

GRAMMAR = """
# Future perfect
**Form:** will have + past participle
• (+) By 10 o'clock, he **will have finished** his work.
• (–) By 10 o'clock, he **will not (won't) have finished** his work.
• (?) **Will** he **have finished** his work by 10 o'clock?

# Use
We use the future perfect for an action that **will be completed before a certain time in the future**.
• She is a student now. By 2038, she **will have got** a job, **bought** a car and **got** married.

# Time expressions
• **by** + time (by 2030, by next year, by Monday, by the time …)
• **before** + time / clause
• **in** + period (in two years' time)

# Common mistakes
• ✗ will have not finish → ✓ will not have finished
• ✗ Will Robin has sold …? → ✓ Will Robin have sold …?
• ✗ will have sleep → ✓ will have slept
"""

G = [
    mc(E, "The future perfect is formed with…", ["will have + past participle", "will + infinitive", "had + past participle", "will be + -ing"]),
    mc(E, "By 10 o'clock, he ___ his work.", ["will have finished", "will finishes", "had finished"]),
    mc(E, "By 2038, she ___ a car.", ["will have bought", "will has bought", "had bought"]),
    mc(E, "By next June, the students ___ all the courses.", ["will have finished", "have finished", "finished"]),
    mc(E, "___ Alice have arrived by six?", ["Will", "Has", "Had"]),
    mc(E, "My sister ___ this book by this evening. She reads slowly.", ["won't have finished", "will have not finished", "won't finished"]),
    mc(E, "We use the future perfect for an action that…", ["will be finished before a time in the future", "happened before another past action", "is happening now"]),
    mc(E, "Which time expression is typical of the future perfect?", ["by next year", "yesterday", "now", "ago"]),
    mc(E, "By 8 o'clock, all the kids will have ___.", ["slept", "sleep", "sleeping"]),
    mc(E, "By the end of this year, I ___ ten novels.", ["will have read", "will read have", "will have reading"]),
    tf(E, "“By next Monday, Robin will have sold his car” means the car will be sold before Monday.", True),

    mc(M, "The government ___ serious measures by next year.", ["will have taken", "will have took", "will taken"]),
    mc(M, "John and Adam ___ all the job by Monday.", ["won't have done", "won't have did", "haven't done"]),
    mc(M, "___ you ___ the report by the deadline?", ["Will / have finished", "Have / will finished", "Will / finish have"]),
    mc(M, "Before I went to bed last night, I ___ a couple of poems.", ["read", "will have read", "will read"], "This is the past, not the future."),
    mc(M, "In two years' time, Ahmed ___ from university.", ["will have graduated", "graduated", "has graduated"]),
    wr(M, "By 2030, Morocco ___ (build) many solar power stations.", ["will have built"]),
    wr(M, "By the time you arrive, we ___ (eat) dinner.", ["will have eaten"]),
    wr(M, "By next month, I ___ (save) enough money for a bike.", ["will have saved"]),
    wr(M, "They ___ (not / finish) the bridge by June. (use the short form)", ["won't have finished"]),
    wr(M, "___ (she / leave) by the time we get there?", ["will she have left"]),

    wr(H, "Correct the sentence: “David will have not finish the project by July.” → David ___ the project by July.", ["will not have finished", "won't have finished"]),
    wr(H, "Correct the sentence: “Will Robin has sell his car by next Monday?” → Will Robin ___ his car by next Monday?", ["have sold"]),
    wr(H, "Correct the sentence: “Will Susan arrive have by dark?” → Will Susan ___ by dark?", ["have arrived"]),
    wr(H, "Correct the sentence: “By 8 o'clock, all the kids will have sleep.” → … will have ___.", ["slept"]),
    mc(H, "Choose the correct sentence.", ["By 2030, scientists will have found new solutions to climate change.", "By 2030, scientists will found new solutions to climate change.", "By 2030, scientists will have find new solutions to climate change."]),
    mc(H, "“She is a student now. By 2038: have a job ✓” → Which sentence is correct?", ["By 2038, she will have had a job.", "By 2038, she has a job.", "By 2038, she had had a job."]),
    mc(H, "Which sentence does NOT need the future perfect?", ["I will call you tomorrow.", "By tomorrow, I will have called the bank.", "By the end of the week, they will have repaired the road."]),
    mc(H, "“He won't have eaten before I come. He'll be hungry.” This sentence is…", ["correct", "wrong: it should be “won't eat”", "wrong: it should be “hasn't eaten”"]),
    mc(H, "Find the mistake: “By 2023, I will living in Casablanca for 7 years.”", ["will living → will have lived", "By → In", "for → since", "There is no mistake."]),
    tf(H, "The future perfect can be used with “by the time + present simple”.", True, "By the time you arrive, we will have eaten."),
]

FUNCTIONS = """
# Cause and effect
• The **cause** answers “Why?” — The **effect** (result) answers “What happened?”
• He had a toothache (cause) → He went to the dentist (effect).

# Linking words of CAUSE
• **because / since / as / for + clause**: Investment is important **because** it creates jobs.
• **because of / due to / owing to / thanks to + noun**: The drought is **due to** lack of rain. Youths can start businesses **thanks to** microcredits (positive cause).

# Linking words of EFFECT
• **so** + clause: Many young people are jobless, **so** banks must help them.
• **therefore / thus / consequently / as a result / as a consequence / because of this / for this reason** (usually at the start of a new sentence, followed by a comma):
Global warming is serious. **Therefore**, we must stop pollution.

# Watch out
• **because** + subject + verb ↔ **because of** + noun: because it rained / because of the rain.
"""

F = [
    mc(E, "Which linking word introduces a CAUSE?", ["because", "therefore", "as a result", "consequently"]),
    mc(E, "Which linking word introduces an EFFECT?", ["therefore", "because", "since", "due to"]),
    mc(E, "Janet cancelled her trip ___ health problems.", ["because of", "because", "thus"], "because of + noun"),
    mc(E, "It was raining yesterday. ___, we didn't play the match.", ["As a result", "Because", "Since"]),
    mc(E, "Alice can't travel with us ___ she has no money.", ["since", "therefore", "because of"]),
    mc(E, "I was too busy, ___ I couldn't go out with my friends.", ["so", "due to", "as"]),
    mc(E, "Recent drought in Morocco is ___ lack of rain.", ["due to", "because", "therefore"]),
    mc(E, "“He had a toothache. He went to the dentist.” What is the CAUSE?", ["He had a toothache.", "He went to the dentist."]),
    tf(E, "“Thanks to” is used for a positive cause.", True),
    tf(E, "“Because of” is followed by a clause (subject + verb).", False, "Because of + noun; because + clause."),

    mc(M, "Youths can start their own businesses ___ microcredits.", ["thanks to", "therefore", "because"]),
    mc(M, "Global warming is a serious problem. ___, we must stop pollution.", ["Therefore", "Because", "Due to"]),
    mc(M, "Morocco is a developing country. ___, it has debts.", ["As a consequence", "Because of", "Since"]),
    mc(M, "Sea level rising is ___ global warming.", ["because of", "because", "so"]),
    mc(M, "We have to preserve our nature ___ we can't live without it.", ["for", "thus", "due to"]),
    mc(M, "Banks are offering microcredits. ___, young people can start their own projects.", ["Thus", "Because", "Owing to"]),
    wr(M, "Complete: Neil couldn't attend the wedding ___ to his illness. (one word)", ["due", "owing"]),
    wr(M, "Complete: The show was cancelled because ___ a technical problem.", ["of"]),
    wr(M, "Complete: Lisa eats unhealthy food. As a ___, she is getting fat.", ["result", "consequence"]),
    wr(M, "Complete: Adam made his dream come true thanks ___ his supportive family.", ["to"]),

    mc(H, "Join: “Neil couldn't attend the wedding. He was ill.” (due to)", ["Neil couldn't attend the wedding due to his illness.", "Neil couldn't attend the wedding due to he was ill.", "Due to Neil couldn't attend the wedding, he was ill."]),
    mc(H, "Join: “The show was cancelled. There was a technical problem.” (thus)", ["There was a technical problem. Thus, the show was cancelled.", "The show was cancelled. Thus, there was a technical problem.", "Thus the show was cancelled a technical problem."]),
    mc(H, "Join: “Lisa is getting fat. She eats unhealthy food.” (because of this)", ["Lisa eats unhealthy food. Because of this, she is getting fat.", "Lisa is getting fat. Because of this, she eats unhealthy food.", "Because of this Lisa eats, she is getting fat."]),
    mc(H, "Which sentence is WRONG?", ["We stayed at home because of it was raining.", "We stayed at home because it was raining.", "We stayed at home because of the rain.", "It was raining, so we stayed at home."]),
    mc(H, "In “Air pollution causes global warming”, global warming is the…", ["effect", "cause", "solution", "purpose"]),
    wr(H, "Rewrite with “because”: Due to the lack of jobs, many youths emigrate. → Many youths emigrate because there ___ a lack of jobs.", ["is", "was"]),
    wr(H, "Rewrite with “so”: Because I was tired, I went to bed. → I was tired, ___ I went to bed.", ["so"]),
    wr(H, "Complete with one linking word: Youness was driving fast. ___, he had an accident.", ["therefore", "consequently", "thus", "so"]),
    mc(H, "Which linking word has the SAME meaning as “consequently”?", ["as a result", "because of", "since", "although"]),
    tf(H, "“Since” can express cause, as in: Morocco is in debt since it is not a developed country.", True),
]

UNIT = unit(4, "Sustainable Development",
            ("Problems & solutions", VOCAB, V),
            ("Future perfect", GRAMMAR, G),
            ("Cause & effect", FUNCTIONS, F))
