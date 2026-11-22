from build_units import *

VOCAB = """
# Qualities of youth (noun → adjective)
• **imagination** → imaginative: forming pictures and ideas in your mind
• **talent / gift** → talented / gifted: a natural ability or skill
• **adventure** → adventurous: loving exciting experiences and risks
• **ambition** → ambitious: having big dreams and working to achieve them
• **passion** → passionate: a strong feeling of love for something
• **creativity** → creative: making new things, using original ideas
• **punctuality** → punctual: respecting time, never late
• **vitality** → vital / energetic: a great deal of energy

# Negative qualities
• **carelessness** → careless: not paying attention, causing problems
• **immaturity** → immature: acting like a child, not responsible
• **impatience** → impatient: hating to wait
• **aggressiveness** → aggressive: tending toward violence

# Generation gap
The **generation gap** is the difference in ideas, values and lifestyle between young and old people, which causes misunderstandings.
• Young people may think old people are: **old-fashioned**, **severe** (too serious, strict), **nosy** (asking about everything), **stingy** (not liking to spend money).
• Old people may think young people are: **irresponsible**, **thoughtless** (not thinking about others or consequences), **strong-headed** / **obstinate** (sticking to their opinions).
"""

V = [
    mc(E, "Which word means “a natural ability or skill”?", ["talent", "ambition", "vitality", "passion"]),
    mc(E, "Which quality is this? 🧗 Someone who loves exciting experiences and taking risks.", ["adventure", "punctuality", "immaturity", "patience"], img="🧗"),
    mc(E, "Which quality is this? ⏰ Always on time, never late.", ["punctuality", "creativity", "carelessness", "vitality"], img="⏰"),
    mc(E, "Which quality is this? 🎨 Making new things and using original ideas.", ["creativity", "aggressiveness", "impatience", "ambition"], img="🎨"),
    mc(E, "Which quality is this? 🏆 Having big dreams and working hard to achieve them.", ["ambition", "imagination", "immaturity", "punctuality"], img="🏆"),
    mc(E, "Which quality is this? ⚡ A great deal of energy; being active.", ["vitality", "carelessness", "passion", "talent"], img="⚡"),
    mc(E, "Which word means “hating to wait”?", ["impatience", "immaturity", "imagination", "punctuality"]),
    mc(E, "What is the adjective of “ambition”?", ["ambitious", "ambitionful", "ambitioned", "ambitive"]),
    mc(E, "Which adjective describes an old person who asks about everything?", ["nosy", "stingy", "punctual", "talented"], img="👀"),
    mc(E, "Which adjective describes someone who doesn't like spending money?", ["stingy", "creative", "nosy", "adventurous"], img="💰"),
    tf(E, "“Immaturity” means acting or behaving like a child.", True),
    tf(E, "“Punctual” means always arriving late.", False, "Punctual = always on time."),

    mc(M, "Najib Mahfouz's ___ helps him come up with stories that nobody else could think of.", ["imagination", "imaginative", "imagine", "imaginary"], "After a possessive (Mahfouz's) we need a noun."),
    mc(M, "As a child, my cousin was extremely ___, always making new games to play.", ["creative", "creativity", "create", "creation"], "After “was extremely” we need an adjective."),
    mc(M, "Winning the art competition was proof of her natural ___.", ["talent", "talented", "talents", "talently"], "After “her natural” we need a noun."),
    mc(M, "Hiking alone in the mountains was a sign of his ___ character.", ["adventurous", "adventure", "adventurer", "adventurously"], "Before a noun (character) we need an adjective."),
    mc(M, "Linda is a very ___ person; she never arrives late.", ["punctual", "punctuality", "punctually", "punctuate"]),
    mc(M, "The teacher warned them that their ___ could cause serious problems.", ["carelessness", "careless", "carelessly", "care-less"]),
    mc(M, "Some people think teenagers can be too ___ to make big decisions.", ["immature", "immaturity", "mature", "immaturely"]),
    mc(M, "Young people think old people are ___ because they don't know how to use modern technologies.", ["old-fashioned", "punctual", "talented", "adventurous"]),
    mc(M, "The difference in ideas and values between young and old people is called the ___.", ["generation gap", "brain drain", "culture shock", "life skill"]),
    wr(M, "Samantha is such a ___ dancer that she won her first award at the age of 10. (talent)", ["talented", "gifted"]),
    wr(M, "His ___ to become a famous scientist keeps him working hard every day. (ambitious)", ["ambition"]),
    wr(M, "We need a ___ idea to make this project stand out. (creativity)", ["creative"]),

    wr(H, "Give the adjective of “vitality”.", ["vital", "energetic"]),
    wr(H, "Give the noun of “aggressive”.", ["aggressiveness", "aggression"]),
    wr(H, "Give the noun of “impatient”.", ["impatience"]),
    wr(H, "Give the noun of “passionate”.", ["passion"]),
    wr(H, "My brother's ___ while driving caused a minor accident. (careless)", ["carelessness"]),
    wr(H, "I admire her because she's ___ even when things take a long time. (patience)", ["patient"]),
    wr(H, "His ___ for music inspired everyone in the room. (passionate)", ["passion"]),
    mc(H, "Old people often say young people are “___” because they stick to their opinions and refuse to change them.", ["obstinate", "stingy", "nosy", "severe"]),
    mc(H, "“He never thinks about how his actions affect others.” He is ___.", ["thoughtless", "punctual", "old-fashioned", "stingy"]),
    mc(H, "Which pair is NOT a correct noun → adjective pair?", ["vitality → vitalous", "talent → talented", "passion → passionate", "ambition → ambitious"]),
]

GRAMMAR = """
# Gerund (verb + -ing)
Use a **gerund**:
• as the **subject** of a sentence: **Dancing** is an excellent activity.
• after **prepositions**: interested in **learning**, apologize for **being** late, before **leaving**, without **making** mistakes, look forward to **meeting**.
• after these verbs: enjoy, finish, avoid, admit, deny, consider, imagine, miss, practise, keep, mind, suggest, risk, discuss.
• after expressions: can't stand, can't help, can't resist, feel like, it's no use, there's no point in, would you mind.

# Infinitive (to + verb)
Use a **to-infinitive**:
• after these verbs: want, decide, promise, refuse, agree, need, hope, pretend, plan, wish, expect, learn, afford, manage, attempt.
• after **adjectives**: It's easy **to say**; I was surprised **to see** you.
• after **question words**: I don't know **what to do**; she knows **how to make** pizza.

# Bare infinitive (verb without “to”)
• after **modals**: can, could, must, should, will, would, may, might — You should **visit** a doctor.
• after **let, make, help**: Her story made everyone **cry**. Let me **speak**.
• after **had better** and **would rather**: You had better **apologize**.

# Verbs followed by both
• Same meaning: like, love, hate, prefer, start, begin, continue (I like **cooking** = I like **to cook**).
• Different meaning:
• **stop** smoking = quit the habit / **stop to** smoke = stop in order to smoke
• **remember / forget doing** = a memory of the past / **remember / forget to do** = a task you must do
"""

G = [
    mc(E, "I enjoy ___ books in my free time.", ["reading", "to read", "read"], "enjoy + gerund"),
    mc(E, "They decided ___ a school here.", ["to build", "building", "build"], "decide + infinitive"),
    mc(E, "You should ___ a doctor.", ["visit", "to visit", "visiting"], "modal + bare infinitive"),
    mc(E, "___ is an excellent form of activity.", ["Dancing", "Dance to", "To dancing"], "A gerund can be the subject."),
    mc(E, "I'm interested in ___ more about Chinese culture.", ["learning", "to learn", "learn"], "preposition + gerund"),
    mc(E, "He promised ___ me back later.", ["to call", "calling", "call"], "promise + infinitive"),
    mc(E, "Her sad story made everyone ___.", ["cry", "to cry", "crying"], "make + object + bare infinitive"),
    mc(E, "My sister knows how ___ pizza.", ["to make", "making", "make"], "question word + infinitive"),
    mc(E, "I haven't finished ___ that book yet.", ["reading", "to read", "read"], "finish + gerund"),
    mc(E, "Some criminals refused ___.", ["to confess", "confessing", "confess"], "refuse + infinitive"),
    tf(E, "After “can't stand” we use a gerund: I can't stand people smoking next to me.", True),

    mc(M, "There's no point in ___ the matter. It's too late.", ["discussing", "to discuss", "discuss"], "there's no point in + gerund"),
    mc(M, "He pretended ___ sick so as to skip work.", ["to be", "being", "be"], "pretend + infinitive"),
    mc(M, "I'm really looking forward to ___ my pen-friend.", ["meeting", "meet", "to meet"], "In “look forward to”, “to” is a preposition → gerund."),
    mc(M, "Have you ever learnt how ___ a plane?", ["to fly", "flying", "fly"]),
    mc(M, "She can't imagine ___ away from her family.", ["living", "to live", "live"], "imagine + gerund"),
    mc(M, "You had better ___ to your mom now.", ["apologize", "to apologize", "apologizing"], "had better + bare infinitive"),
    mc(M, "Don't forget ___ the door before leaving.", ["to lock", "locking", "lock"], "forget to do = a task you must not forget"),
    mc(M, "He stopped ___. He doesn't smoke anymore.", ["smoking", "to smoke", "smoke"], "stop + gerund = quit the habit"),
    wr(M, "They forgot ___ the notepad. (bring)", ["to bring"], "forget to do something = not remember a task"),
    wr(M, "I can't imagine ___ from home. (work)", ["working"]),
    wr(M, "This sentence sounds really hard ___. (read)", ["to read"], "adjective + infinitive"),
    wr(M, "___ yoga is my favourite morning activity. (practise)", ["practising", "practicing"], "gerund as subject"),

    mc(H, "Choose the correct sentence.", ["He doesn't know how to prepare sushi.", "He doesn't know how preparing sushi.", "He doesn't know how prepare sushi."]),
    mc(H, "Choose the correct sentence.", ["Finish your homework before going out!", "Finish your homework before to go out!", "Finish your homework before go out!"], "preposition + gerund"),
    mc(H, "Choose the correct sentence.", ["We've decided to play tennis this afternoon.", "We've decided playing tennis this afternoon.", "We've decided play tennis this afternoon."]),
    mc(H, "“I remember visiting my grandparents.” This means…", ["I have a memory of the visit.", "I must not forget to visit them.", "I stopped visiting them."], "remember + -ing = a memory of the past"),
    mc(H, "“We stopped to call Alex.” This means…", ["We stopped what we were doing in order to call Alex.", "We don't call Alex anymore.", "We forgot to call Alex."]),
    wr(H, "Correct the mistake: “Today, we're going to practise to sing.” → Today, we're going to practise ___.", ["singing"], "practise + gerund"),
    wr(H, "Correct the mistake: “They should to visit a doctor.” → They should ___ a doctor.", ["visit"], "modal + bare infinitive"),
    wr(H, "You should help your sister ___ the exercise. (do)", ["do", "to do"], "help + (to) infinitive"),
    wr(H, "I would rather ___ to the cinema than to the zoo. (go)", ["go"], "would rather + bare infinitive"),
    wr(H, "We look forward to ___ you soon. (meet)", ["meeting"]),
    tf(H, "“Johan prefers drawing portraits” and “Johan prefers to draw portraits” have the same meaning.", True, "prefer can take both with the same meaning."),
]

FUNCTIONS = """
# Making requests
• Could you please + infinitive …?
• Can you please + infinitive …?
• Could you possibly + infinitive …?
• Can I ask you to + infinitive …?
• Do you think you could + infinitive …?
• Would it be possible for you to + infinitive …?
• Would you be so kind as to + infinitive …?
• I wonder if you could + infinitive …
• I'd appreciate it if you could + infinitive …
• I'd like you to + infinitive …
• **Would you mind + gerund …?** (Would you mind opening the window?)

# Accepting requests
• Yes, sure. / Of course! / With pleasure! / Why not? / No problem.

# Refusing requests
• I'm awfully sorry, but … / I'm afraid I can't. / Sorry, I wish I could, but … / No, I can't.

# Tip
To answer “Would you mind …?” positively, say **“No, not at all.”** (= I don't mind, I'll do it).
"""

F = [
    mc(E, "Which expression is used to MAKE a request?", ["Could you please help me?", "With pleasure!", "I'm afraid I can't.", "Why not?"]),
    mc(E, "Which expression ACCEPTS a request?", ["With pleasure!", "I'm afraid I can't.", "I wish I could, but…", "I'm awfully sorry."]),
    mc(E, "Which expression REFUSES a request?", ["I'm afraid I can't.", "Of course!", "Yes, sure.", "With pleasure!"]),
    mc(E, "Would you mind ___ the window?", ["opening", "to open", "open"], "Would you mind + gerund"),
    mc(E, "Could you please ___ me the meeting agenda?", ["email", "emailing", "to email"], "Could you please + bare infinitive"),
    mc(E, "“Can you lend me your pen?” — “___ Here you are.”", ["Of course!", "I'm afraid I can't.", "No, I can't."]),
    tf(E, "“Sorry, I wish I could” is a way of accepting a request.", False, "It is a polite refusal."),
    tf(E, "“I'd appreciate it if you could…” is a polite way to make a request.", True),
    mc(E, "Which request is the MOST polite?", ["Would you be so kind as to help me?", "Help me.", "You must help me.", "I want help."]),
    mc(E, "“Can I ask you to close the door?” — “___”", ["Sure, no problem.", "You're right.", "I don't agree."]),

    mc(M, "Would you be so kind as ___ me a hand?", ["to give", "giving", "give"], "be so kind as + to-infinitive"),
    mc(M, "I'd appreciate it if you ___ turn down the music.", ["could", "can to", "would to"]),
    mc(M, "Would it be possible for you ___ the report by Friday?", ["to finish", "finishing", "finish"]),
    mc(M, "Do you think you could ___ me the salt?", ["pass", "passing", "to pass"]),
    mc(M, "“Could you please email me the meeting agenda before tomorrow?” — Best answer:", ["Sure, I'll send it right away.", "Thank you so much.", "I don't share the same view."]),
    mc(M, "“Would you mind waiting a few minutes?” — You agree to wait. You say:", ["No, not at all.", "Yes, I mind.", "I'm afraid I can't.", "Yes, a lot."], "“Not at all” = I don't mind."),
    mc(M, "Your friend asks for 100 MAD but you have no money. You say:", ["I'm awfully sorry, but I'm penniless.", "With pleasure!", "Yes, sure, here you are.", "Why not?"]),
    mc(M, "Your classmate needs help with an exercise and you accept. You say:", ["Of course! Let me see it.", "I'm afraid I can't.", "Sorry, I wish I could.", "No, I can't."]),
    wr(M, "Complete the request: Would you mind ___ (lend) me your notes?", ["lending"]),
    wr(M, "Complete the request: Could you possibly ___ (call) me later?", ["call"]),

    wr(H, "Complete: I wonder if you ___ help me with my homework. (modal)", ["could", "can"]),
    wr(H, "Complete: Would it be ___ for you to drive me to school? (one word)", ["possible"]),
    wr(H, "Complete: I'd ___ it if you could lower your voice. (one word)", ["appreciate"]),
    wr(H, "Complete the polite refusal: I'm ___ I can't. (one word)", ["afraid"]),
    mc(H, "You want your teacher to explain the lesson again. Which is the most appropriate?", ["Would you be so kind as to explain it again?", "Explain it again!", "You must explain it again.", "Why don't you explain it again?"], "Use a very polite request with a teacher."),
    mc(H, "Which sentence is grammatically WRONG?", ["Would you mind to help me?", "Could you help me?", "Would you mind helping me?", "Do you think you could help me?"], "Would you mind + gerund (helping)."),
    mc(H, "“Can you take my place at the meeting?” — “Sorry, I wish I could, but I'm travelling tomorrow.” The speaker is…", ["refusing the request politely", "accepting the request", "making a request", "giving advice"]),
    mc(H, "Which response does NOT fit the request “Could you lend me your dictionary?”", ["I completely agree with you.", "Sure, here it is.", "I'm afraid I need it now.", "With pleasure!"]),
    tf(H, "“Would you mind closing the door?” — “Yes, I would.” means the person agrees to close the door.", False, "“Yes, I would (mind)” is a refusal."),
    mc(H, "Choose the correct request.", ["Can I ask you to turn off your phone?", "Can I ask you turning off your phone?", "Can I ask you turn off your phone?"]),
]

UNIT = unit(1, "The Gifts of Youth",
            ("Qualities of youth & generation gap", VOCAB, V),
            ("Gerund & infinitive", GRAMMAR, G),
            ("Making and responding to requests", FUNCTIONS, F))
