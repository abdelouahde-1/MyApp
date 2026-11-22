from build_units import *

VOCAB = """
# Key concepts
• **culture**: the way of life of a group of people: customs, beliefs, language and traditions
• **value**: a principle or belief that is important to people
• **cultural values**: the shared principles of a culture

# Cultural values
• **tolerance**: respecting other people's opinions and beliefs (opposite: **intolerance**)
• **coexistence**: living together in harmony with different customs
• **cooperation**: helping one another, working together towards the same goal (opposite: individuality)
• **hospitality**: generous and friendly behaviour towards guests (opposite: unfriendliness)
• **altruism**: caring about the needs of other people; selflessness (opposite: **selfishness**)
• **initiative**: the ability to take the first step, to act before others
• **equality**: treating people the same way (opposite: inequality)
• **equity**: giving people what they need; fair treatment that considers different abilities (opposite: **injustice**)
• **love** (opposite: hatred) · **solidarity** · **generosity** · **respect**

# Word families
• tolerate → tolerance → tolerant · cooperate → cooperation → cooperative
• coexist → coexistence · hospitable → hospitality · altruist → altruism → altruistic
"""

V = [
    mc(E, "Respecting other people's opinions and beliefs is…", ["tolerance", "selfishness", "initiative", "hatred"], img="🤝"),
    mc(E, "Generous behaviour towards guests is…", ["hospitality", "intolerance", "equity", "individuality"], img="🫖"),
    mc(E, "Helping one another and working together is…", ["cooperation", "selfishness", "initiative", "intolerance"], img="👥"),
    mc(E, "Caring about the needs of other people is…", ["altruism", "selfishness", "hatred", "injustice"], img="❤️"),
    mc(E, "Taking the first step and acting before others is…", ["initiative", "hospitality", "coexistence", "equity"], img="🚀"),
    mc(E, "Living together in harmony with different customs is…", ["coexistence", "intolerance", "individuality", "hatred"], img="🕊️"),
    mc(E, "Treating people the same way is…", ["equality", "selfishness", "initiative", "hospitality"], img="⚖️"),
    mc(E, "Giving people what they need is…", ["equity", "equality", "hatred", "individuality"]),
    mc(E, "What is the opposite of “altruism”?", ["selfishness", "tolerance", "hospitality", "love"]),
    mc(E, "What is the opposite of “love”?", ["hatred", "tolerance", "equity", "cooperation"]),
    tf(E, "Culture includes customs, beliefs, language and traditions.", True),
    tf(E, "“Intolerance” means accepting other people's beliefs.", False, "Intolerance = refusing to accept views that differ from yours."),

    mc(M, "___ can be defined as a way of life for a group of people.", ["Culture", "Equity", "Initiative", "Altruism"]),
    mc(M, "___ can be described as selflessness, generosity and kindness.", ["Altruism", "Intolerance", "Individuality", "Initiative"]),
    mc(M, "___ refers to the ability or opportunity to act before others do.", ["Initiative", "Hospitality", "Coexistence", "Equality"]),
    mc(M, "___ refers to the friendly and generous reception of guests.", ["Hospitality", "Cooperation", "Tolerance", "Equity"]),
    mc(M, "___ means refusing to accept views or beliefs that differ from your own.", ["Intolerance", "Tolerance", "Altruism", "Equity"]),
    mc(M, "Morocco is known for its ___. Tourists always remember Moroccan generosity.", ["hospitality", "selfishness", "individuality", "injustice"]),
    mc(M, "___ is different from equality; it is about giving everyone what they need to succeed.", ["Equity", "Hatred", "Initiative", "Culture"]),
    mc(M, "Good deeds always begin with an ___. One has to have the courage to start first.", ["initiative", "intolerance", "hospitality", "equality"]),
    mc(M, "Before people with different beliefs can coexist, they need to ___ each other.", ["tolerate", "hate", "ignore", "compete"]),
    wr(M, "The opposite of “cooperation” is ___ (i__________).", ["individuality", "individualism"]),
    wr(M, "The opposite of “equity” is ___ (i________).", ["injustice", "inequity"]),

    wr(H, "Give the adjective of “tolerance”.", ["tolerant"]),
    wr(H, "Give the verb of “cooperation”.", ["cooperate"]),
    wr(H, "Give the adjective of “hospitality”.", ["hospitable"]),
    wr(H, "Give the adjective of “altruism”.", ["altruistic"]),
    wr(H, "Give the verb of “coexistence”.", ["coexist"]),
    wr(H, "___ refers to participating and working together towards the same objective. (one word)", ["cooperation"]),
    wr(H, "___ is all about sacrificing yourself to help those in need. (one word)", ["altruism"]),
    mc(H, "Which pair of antonyms is WRONG?", ["hospitality ↔ equity", "tolerance ↔ intolerance", "altruism ↔ selfishness", "love ↔ hatred"]),
    mc(H, "“The teacher gives extra time to a student with a disability.” This illustrates…", ["equity", "equality", "hospitality", "initiative"]),
    mc(H, "Which word is NOT a cultural value?", ["selfishness", "tolerance", "solidarity", "hospitality"]),
    tf(H, "Equality and equity mean exactly the same thing.", False, "Equality = same treatment; equity = treatment according to needs."),
]

GRAMMAR = """
# Phrasal verbs
A **phrasal verb** = verb + particle (preposition/adverb). The meaning is often different from the verb alone: put (place) / put on (wear) / put off (postpone).

# Break
• **break up**: end a relationship · **break down**: stop working (a machine)
• **break into**: enter by force to steal · **break out**: start suddenly (war, fire, disease)

# Look
• **look after**: take care of · **look up**: search for a word in a dictionary
• **look for**: try to find · **look back on**: remember the past
• **look down on**: think you are better than others · **look forward to + -ing**: wait with pleasure

# More phrasal verbs
• **apply for**: ask officially for a job/scholarship · **fill in**: complete a form
• **go through**: experience something difficult · **come across**: meet/find by chance
• **bring about**: cause · **find out**: discover
• **call off**: cancel · **put off**: postpone · **name after**: give someone's name to
• **stand for**: represent (abbreviations) · **call for**: demand publicly
• **drop by**: visit briefly · **pass away**: die
• **carry on**: continue · **bring up**: raise a child
• **lie down**: rest in a lying position · **stay up**: not go to bed
• **put on**: wear / dress · **give up**: stop doing
"""

G = [
    mc(E, "My car ___ when I was going to work. It stopped working.", ["broke down", "broke into", "broke up"]),
    mc(E, "A thief ___ the house and stole my money.", ["broke into", "broke down", "broke out"]),
    mc(E, "A huge fire has ___ in the forest near us.", ["broken out", "broken into", "broken up"]),
    mc(E, "My aunt loves to ___ my baby daughter. (take care of)", ["look after", "look for", "look up"]),
    mc(E, "I'm ___ my keys. Have you seen them?", ["looking for", "looking after", "looking down on"]),
    mc(E, "Whenever I don't understand a word, I ___ in the dictionary.", ["look it up", "look it after", "look it for"]),
    mc(E, "We ___ the match because of the rain. (cancelled)", ["called off", "called for", "carried on"]),
    mc(E, "My grandfather ___ last year. May he rest in peace.", ["passed away", "dropped by", "stayed up"]),
    mc(E, "After school, I ___ my pyjamas.", ["put on", "put off", "put away"]),
    mc(E, "“Don't stop, ___!”", ["carry on", "call off", "pass away"]),
    tf(E, "“Stand for” means “represent”, as in: UN stands for United Nations.", True),
    tf(E, "“Stay up” means “go to bed early”.", False, "Stay up = not go to bed."),

    mc(M, "Despite their love, they ___ for unknown reasons.", ["broke up", "broke down", "broke into"]),
    mc(M, "Yesterday, a war ___ in the city. Many people were killed.", ["broke out", "broke up", "broke down"]),
    mc(M, "It's not good to ___ people because we are all equal.", ["look down on", "look after", "look up"]),
    mc(M, "I have just got my BAC, and I'm ___ starting higher education.", ["looking forward to", "looking back on", "looking for"]),
    mc(M, "Whenever my mom checks her pictures, she ___ the good old days.", ["looks back on", "looks down on", "looks for"]),
    mc(M, "Yesterday, I was eating out when I ___ Johnny, an old friend.", ["came across", "came off", "came up"]),
    mc(M, "When Sarah's parents died, her uncle ___ her ___.", ["brought / up", "called / off", "put / on"]),
    mc(M, "Air pollution ___ global warming.", ["brings about", "breaks up", "passes away"]),
    mc(M, "Homeless people ___ hard times.", ["go through", "drop by", "fill in"]),
    mc(M, "To ___ this job, you must ___ this application form.", ["apply for / fill in", "fill in / apply for", "call off / look up"]),
    wr(M, "COVID-19 stands ___ Corona Virus Disease 2019.", ["for"]),
    wr(M, "When I was sick last week, Sam dropped ___ to see me.", ["by"]),

    wr(H, "Replace with a phrasal verb: The meeting was CANCELLED by the boss. → The meeting was ___ by the boss.", ["called off"]),
    wr(H, "Replace with a phrasal verb: We shall soon DISCOVER whether the new boss is good. → We shall soon ___ …", ["find out"]),
    wr(H, "Replace with a phrasal verb: Children shouldn't REMAIN AWAKE late at night. → Children shouldn't ___ late.", ["stay up"]),
    wr(H, "Replace with a phrasal verb: My father used to REST ON THE BED after work. → My father used to ___ after work.", ["lie down"]),
    wr(H, "Complete: All the girls born on that day were named ___ Nawal El Moutawakel.", ["after"]),
    wr(H, "Complete: The winner of a peace prize should not call ___ war.", ["for"]),
    mc(H, "Which phrasal verb means “postpone”?", ["put off", "put on", "put up", "put away"]),
    mc(H, "Which phrasal verb means “raise a child”?", ["bring up", "bring about", "break up", "look up"]),
    mc(H, "Choose the correct sentence.", ["I'm looking forward to meeting you.", "I'm looking forward to meet you.", "I'm looking forward meeting you."], "look forward to + -ing"),
    mc(H, "“El Saadawi calls for women's rights in her books.” “Calls for” means…", ["demands", "cancels", "visits", "remembers"]),
]

FUNCTIONS = """
# Complaining
• I'm sorry to say it, but you …
• I'm not satisfied with the way you …
• I've been patient long enough, but …
• Enough is enough!
• I'd like to make a complaint about …
• I'm afraid there is a problem with …

# Apologizing
• I'm (very / so / terribly) sorry.
• I do apologize. / I apologize for + -ing …
• Sorry about that.
• Please accept my apologies.
• It won't happen again.

# Accepting an apology
• It's OK. / That's all right.
• Never mind!
• It's not important.
• Don't worry about it.
• No problem.
"""

F = [
    mc(E, "Which expression is used to COMPLAIN?", ["I'm not satisfied with the way you…", "I do apologize.", "Never mind!", "Don't worry about it."]),
    mc(E, "Which expression is used to APOLOGIZE?", ["I do apologize.", "Enough is enough!", "Never mind!", "It's OK."]),
    mc(E, "Which expression ACCEPTS an apology?", ["Never mind!", "I'm very sorry.", "Enough is enough!", "I'm not satisfied."]),
    mc(E, "“I'm sorry to say it, but I ordered a pizza, not a salad!” The lady is…", ["complaining", "apologizing", "accepting an apology", "making a request"], img="🍕"),
    mc(E, "“We're so sorry. It was a mistake. We'll change it now.” The waiter is…", ["apologizing", "complaining", "giving advice", "refusing"]),
    mc(E, "“It's OK. Don't worry about it.” The speaker is…", ["accepting an apology", "complaining", "apologizing", "asking for clarification"]),
    tf(E, "“Enough is enough!” is used to complain.", True),
    tf(E, "“Sorry about that” is used to accept an apology.", False, "It is used to apologize."),
    mc(E, "I apologize ___ being late to class.", ["for", "to", "about"]),
    mc(E, "I'm not ___ with the way you treat customers.", ["satisfied", "satisfying", "satisfaction"]),

    mc(M, "“I've been patient long enough, but the service in your restaurant is really slow.” — Best answer:", ["I'm terribly sorry. Your order is coming right now.", "Never mind!", "Enough is enough!", "I'm not satisfied."]),
    mc(M, "“I'm very sorry I broke your phone.” — Best answer:", ["Don't worry about it.", "I'm not satisfied with you.", "I do apologize.", "Enough is enough."]),
    mc(M, "“We're so sorry. We will give you another laptop right now.” What came before?", ["The laptop you sold me doesn't work. I'm not satisfied.", "Could you lend me your laptop?", "I apologize for the delay.", "Never mind."]),
    mc(M, "“I am sorry to say it, but this room is too noisy for studying.” The function is…", ["complaining", "apologizing", "giving opinion", "making a request"]),
    mc(M, "“I apologize for being late to class.” The function is…", ["apologizing", "complaining", "expressing agreement", "asking for opinion"]),
    mc(M, "“Please send me the document by email.” The function is…", ["making a request", "complaining", "apologizing", "expressing disagreement"]),
    wr(M, "Complete the apology: Please accept my ___. (noun)", ["apologies", "apology"]),
    wr(M, "Complete the complaint: I've been patient long ___, but …", ["enough"]),
    wr(M, "Complete the acceptance: Never ___!", ["mind"]),
    wr(M, "Complete: I apologize for ___ (make) so much noise.", ["making"]),

    mc(H, "Your neighbour's music is loud at midnight for the third time. The most appropriate complaint is:", ["I'm sorry to say it, but your music is too loud. I can't sleep.", "Never mind, it's OK.", "I do apologize for the noise.", "Could you clarify that, please?"]),
    mc(H, "You arrived very late to your friend's party. The most appropriate apology is:", ["I'm terribly sorry I'm late. It won't happen again.", "Enough is enough!", "I'm not satisfied with your party.", "Don't worry about it."]),
    mc(H, "Which response is NOT appropriate after “I'm so sorry I forgot your birthday.”?", ["I'm not satisfied with the way you cook.", "That's all right.", "Don't worry about it.", "Never mind."]),
    mc(H, "Match: “That's absolutely true.” →", ["expressing agreement", "complaining", "apologizing", "making a request"]),
    mc(H, "Match: “How do you feel about working from home?” →", ["asking for opinion", "giving opinion", "complaining", "apologizing"]),
    mc(H, "Match: “I don't think that's the best solution.” →", ["expressing disagreement", "accepting an apology", "making a request", "asking for clarification"]),
    wr(H, "Rewrite as an apology: “I'm late.” → I'm sorry ___ being late.", ["for", "about"]),
    wr(H, "Complete the complaint: I'd like to make a ___ about the service.", ["complaint"]),
    wr(H, "Give the verb of “complaint”.", ["complain"]),
    wr(H, "Give the noun of “apologize”.", ["apology"]),
]

UNIT = unit(6, "Cultural Values",
            ("The values of our culture", VOCAB, V),
            ("Phrasal verbs", GRAMMAR, G),
            ("Complaining & apologizing", FUNCTIONS, F))
