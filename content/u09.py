from build_units import *

VOCAB = """
# Science vs technology
• **Science**: a methodical way of gaining knowledge about the world through **observation** and **experiments**; investigating natural phenomena.
• **Technology**: the **practical application** of scientific knowledge; inventing tools and materials that make life easier (often for industrial or commercial use).
• Science produces knowledge; technology uses it to build tools.

# Branches → people
• science → **scientist** · research → **researcher**
• biology → **biologist** · chemistry → **chemist** · physics → **physicist**
• mathematics → **mathematician** · astronomy (stars & planets) → **astronomer**
• computer science → **programmer** / computer scientist · inventing → **inventor**
• space travel → **astronaut** (Neil Armstrong)

# Famous names
• Marie Curie (chemist / physicist, discovered radium) · Albert Einstein (physicist)
• Charles Darwin (biologist) · Thomas Edison (inventor) · Alan Turing (computer scientist)

# Useful words
• **invention / to invent** · **discovery / to discover** · **experiment** · **laboratory**
• **artificial intelligence (AI)** · **robot** · **device** · **innovation**
"""

V = [
    mc(E, "A person who creates new machines or tools is an…", ["inventor", "astronaut", "biologist", "astronomer"], img="💡"),
    mc(E, "The science of stars and planets is…", ["astronomy", "biology", "chemistry", "mathematics"], img="🔭"),
    mc(E, "A person who travels into space is an…", ["astronaut", "astronomer", "inventor", "chemist"], img="👨‍🚀"),
    mc(E, "The study of living things (plants, animals) is…", ["biology", "physics", "astronomy", "chemistry"], img="🧬"),
    mc(E, "A person who works in chemistry is a…", ["chemist", "chemistry", "chemical", "chemistrian"], img="⚗️"),
    mc(E, "A person who writes computer programs is a…", ["programmer", "physicist", "biologist", "astronaut"], img="💻"),
    mc(E, "A machine that can do tasks automatically, sometimes like a human, is a…", ["robot", "telescope", "microscope", "calculator"], img="🤖"),
    mc(E, "A person who studies mathematics is a…", ["mathematician", "mathematics", "mathematic", "mathemist"], img="➗"),
    mc(E, "“The practical application of scientific knowledge” is…", ["technology", "science", "biology", "astronomy"]),
    mc(E, "“A methodical way of gaining knowledge through observation and experiments” is…", ["science", "technology", "invention", "programming"]),
    tf(E, "Technology is the practical application of science.", True),
    tf(E, "An astronomer is a person who travels into space.", False, "That's an astronaut; an astronomer studies stars and planets."),

    mc(M, "Marie Curie was a famous ___ who discovered radium.", ["scientist", "science", "scientific", "astronaut"]),
    mc(M, "Albert Einstein was a brilliant ___.", ["physicist", "physics", "physical", "physic"]),
    mc(M, "Charles Darwin was an important ___.", ["biologist", "biology", "biological", "biologic"]),
    mc(M, "Thomas Edison was a famous ___.", ["inventor", "inventing", "invention", "invent"]),
    mc(M, "They are doing ___ on artificial intelligence.", ["research", "researcher", "researching", "researches"]),
    mc(M, "Neil Armstrong was an ___.", ["astronaut", "astronomy", "astronomer", "astrology"]),
    mc(M, "She wants to become a ___ in the future.", ["mathematician", "mathematics", "mathematical", "math"]),
    mc(M, "___ explains how things move and interact.", ["Physics", "Physicist", "Physical", "Physician"]),
    mc(M, "A ___ spends a lot of time collecting information.", ["researcher", "research", "researching", "researched"]),
    mc(M, "“It is the process of designing and building tools for practical human use.” This describes…", ["technology", "science", "astronomy", "biology"]),
    wr(M, "My brother studies computer ___ at university.", ["science"]),
    wr(M, "AI stands for ___ intelligence.", ["artificial"]),

    wr(H, "Give the person: physics → ___", ["physicist"]),
    wr(H, "Give the person: chemistry → ___", ["chemist"]),
    wr(H, "Give the person: astronomy (studying stars) → ___", ["astronomer"]),
    wr(H, "Give the noun of “invent” (the thing invented).", ["invention"]),
    wr(H, "Give the noun of “discover”.", ["discovery"]),
    wr(H, "Give the adjective of “science”.", ["scientific"]),
    mc(H, "Which statement describes SCIENCE, not technology?", ["It's about producing knowledge about the world.", "It's the application of science for industrial objectives.", "It's designing and building tools.", "It's inventing devices that make life easier."]),
    mc(H, "Which pair is WRONG?", ["astronomy → astronaut", "biology → biologist", "physics → physicist", "science → scientist"], "astronomy → astronomer"),
    mc(H, "Which word is the odd one out?", ["laboratory", "biologist", "chemist", "physicist"], "The others are people."),
    tf(H, "Alan Turing is known as a pioneer of computer science.", True),
]

GRAMMAR = """
# Wishes
**Present wish** (regret about a present situation): **wish / if only + past simple** (were for all persons)
• Reality: I don't have a sister. → I **wish** I **had** a sister.
• Reality: I don't live in a big city. → **If only** I **lived** in a big city.
• Reality: Adam can't swim. → Adam wishes he **could** swim.
• Positive ↔ negative: Leila spends too much time online → Leila wishes she **didn't spend** so much time online.

**Past wish** (regret about the past): **wish / if only + past perfect (had + pp)**
• Reality: I didn't revise. → I wish I **had revised** for the exam.
• Reality: I failed the exam. → I wish I **hadn't failed** the exam.

# Conditional type 3
For **impossible / unreal situations in the past** (the past can't be changed).
**Form:** If + **past perfect**, subject + **would have + past participle**
• Reality: I didn't study, so I didn't pass. → If I **had studied** harder, I **would have passed** the exam.
• The if-clause can come second (no comma): He **wouldn't have missed** the train if he **had left** earlier.
• Remember to reverse positive ↔ negative: She didn't wake up early → If she **had woken** up early, she **wouldn't have missed** the bus.
"""

G = [
    mc(E, "I don't have a sister. → I wish I ___ a sister.", ["had", "have", "will have"], "present wish → past simple"),
    mc(E, "I didn't revise. → I wish I ___ for the exam.", ["had revised", "revised", "revise"], "past wish → past perfect"),
    mc(E, "If I had studied harder, I ___ the exam.", ["would have passed", "will pass", "would pass"]),
    mc(E, "If Salma had studied harder, ___ the exam.", ["she would have passed", "she will pass", "she would passed"]),
    mc(E, "They would have caught the train ___ earlier.", ["if they had left", "if they had leave", "if they have left"]),
    mc(E, "I would have helped him ___ me.", ["if he had called", "if he had call", "if he have call"]),
    mc(E, "If I had known about the party, ___.", ["I would have gone", "I will go", "I would have go"]),
    mc(E, "Conditional type 3 is used for…", ["unreal situations in the past", "real situations in the future", "general truths", "present habits"]),
    mc(E, "I don't speak English fluently. → I wish I ___ English fluently.", ["spoke", "speak", "had spoke"]),
    mc(E, "Present wishes use…", ["wish + past simple", "wish + present simple", "wish + will"]),
    tf(E, "“If only” has the same meaning as “I wish”.", True),
    tf(E, "In conditional 3, the if-clause uses the present perfect.", False, "It uses the past perfect (had + pp)."),

    mc(M, "Adam doesn't know how to swim. → Adam wishes he ___ how to swim.", ["knew", "knows", "had know"]),
    mc(M, "We live far away from our school. → If only we ___ near our school.", ["lived", "live", "had live"]),
    mc(M, "They missed the bus this morning. → They wish they ___ the bus.", ["hadn't missed", "didn't miss", "don't miss"]),
    mc(M, "Leila spends too much time on social media. → Leila wishes she ___ so much time on social media.", ["didn't spend", "doesn't spend", "hadn't spend"]),
    mc(M, "Salma didn't go to the wedding. → Salma wishes she ___ to the wedding.", ["had gone", "went", "goes"]),
    mc(M, "He didn't leave earlier, so he missed the train. → He ___ the train if he had left earlier.", ["wouldn't have missed", "wouldn't miss", "won't miss"]),
    wr(M, "I failed the exam last week. → I wish I ___ the exam.", ["hadn't failed", "had not failed", "had passed"]),
    wr(M, "These children work in fields. → I wish these children ___ in fields.", ["didn't work", "did not work"]),
    wr(M, "They don't have enough money to travel. → They wish they ___ enough money to travel.", ["had"]),
    wr(M, "She didn't know about the meeting, so she didn't attend. → If she had known about the meeting, she would have ___.", ["attended"]),

    wr(H, "Rewrite: She didn't wake up early. So, she missed the bus. → If she had woken up early, she ___ the bus.", ["wouldn't have missed", "would not have missed"]),
    wr(H, "Rewrite: They didn't study for the test. As a result, they failed it. → If they ___ for the test, they wouldn't have failed it.", ["had studied"]),
    wr(H, "Rewrite: I didn't save the document, so I lost my work. → If I had saved the document, I ___ my work.", ["wouldn't have lost", "would not have lost"]),
    wr(H, "Rewrite: The government doesn't invest enough in renewable energy. → If only the government ___ more in renewable energy.", ["invested"]),
    wr(H, "Rewrite: I forgot to reply to the email yesterday. → I wish I ___ to the email.", ["had replied", "hadn't forgotten to reply", "had not forgotten to reply"]),
    wr(H, "Rewrite: He said something rude to his friend. → If only he ___ something rude.", ["hadn't said", "had not said"]),
    mc(H, "Rewrite: “The company didn't update its software. Therefore, it was hacked.”", ["If the company had updated its software, it wouldn't have been hacked.", "If the company updated its software, it wouldn't be hacked.", "If the company had updated its software, it would have been hacked."]),
    mc(H, "Rewrite: “We didn't go to the concert because we didn't know about it.”", ["If we had known about the concert, we would have gone.", "If we knew about the concert, we would go.", "If we had known about the concert, we wouldn't have gone."]),
    mc(H, "Find the mistake: “If I would have known, I would have come.”", ["would have known → had known", "would have come → had come", "If → Unless", "There is no mistake."]),
    mc(H, "Which sentence is correct?", ["I wish I were taller.", "I wish I am taller.", "I wish I will be taller.", "I wish I had be taller."]),
]

FUNCTIONS = """
# Expressing certainty
• I'm sure (that) … · I'm (absolutely) certain (that) …
• I'm positive (that) … · I have no doubt(s) about it. · There's no doubt that …
• Definitely! / Certainly! / Without a doubt.

# Expressing uncertainty
• I'm not (so) sure. · I'm not certain. · I doubt it. / I doubt that …
• I don't know for sure. · I have my doubts. · It's possible, but …
• Maybe / perhaps … · I don't believe it's true that …

# Example
Alex: I'm sure AI will change our lives. (certainty)
Jordan: I'm not so sure. I still have my doubts. (uncertainty)
"""

F = [
    mc(E, "Which expression shows CERTAINTY?", ["I'm absolutely sure.", "I doubt it.", "I'm not so sure.", "I don't know for sure."]),
    mc(E, "Which expression shows UNCERTAINTY?", ["I don't know for sure.", "I'm positive.", "I have no doubts about it.", "I'm certain."]),
    mc(E, "“Do you think humans will live on Mars soon?” — You are not sure. You say:", ["I don't know for sure.", "I'm sorry.", "My goodness!"], img="🪐"),
    mc(E, "“I'm certain AI will handle many daily tasks soon.” The speaker is…", ["certain", "uncertain", "apologizing", "complaining"], img="🤖"),
    mc(E, "“I'm not so sure. It's just a passing wave.” The speaker is…", ["uncertain", "certain", "giving advice", "responding to good news"]),
    tf(E, "“I have no doubts about it” expresses certainty.", True),
    tf(E, "“I doubt it” expresses certainty.", False),
    mc(E, "I'm ___ that renewable energy is the best solution.", ["certain", "doubt", "maybe", "unsure of"]),
    mc(E, "I ___ it when people say machines will be smarter than humans.", ["doubt", "sure", "certain", "positive"]),
    mc(E, "“Will robots replace teachers?” — “___ , but I'm not sure.”", ["Maybe", "Definitely", "Certainly", "Without a doubt"]),

    mc(M, "“I am certain that renewable energy will replace fossil fuels.” — Best response:", ["I totally agree.", "My apologies.", "Yes, with pleasure."]),
    mc(M, "“Sorry to bother you, but this phone isn't working.” — Best response:", ["We're so sorry.", "Good news.", "I share the same view."], "This is a complaint."),
    mc(M, "“Sorry, I accidentally deleted your research file.” — Best response:", ["That's okay.", "You're right.", "I am pretty sure."], "Accepting an apology."),
    mc(M, "“What software should I use for my presentation?” — Best response:", ["Try using PowerPoint.", "I disagree.", "Yes, for sure."], "Giving advice."),
    mc(M, "“I finally invented the robot and it works perfectly!” — Best response:", ["That's awesome!", "Sorry to hear that.", "I can't."], "Good news."),
    mc(M, "“My laptop was hacked yesterday.” — Best response:", ["I'm sorry to hear that.", "Sounds perfect.", "Yes, please."], "Bad news."),
    wr(M, "Complete the expression of certainty: I'm ___ sure. (a__________)", ["absolutely", "pretty", "quite", "completely"]),
    wr(M, "Complete the expression of uncertainty: I don't know for ___.", ["sure", "certain"]),
    wr(M, "Complete the expression of uncertainty: I still have my ___.", ["doubts"]),
    wr(M, "Complete the expression of certainty: I have no ___ about it.", ["doubt", "doubts"]),

    mc(H, "“Do you think people will travel in flying cars soon?” — Express CERTAINTY:", ["I'm positive they will.", "I doubt it.", "I'm not so sure.", "Perhaps."]),
    mc(H, "“Some people say robots will replace teachers.” — Express UNCERTAINTY:", ["I don't know for sure if that will happen.", "I'm absolutely certain they will.", "There's no doubt about it.", "Definitely!"]),
    mc(H, "“AI is transforming healthcare every day.” — Express LACK OF UNDERSTANDING:", ["Sorry, I'm not sure I know what you mean.", "I'm certain.", "Congratulations!", "That's okay."]),
    mc(H, "“Can you help me install this software, please?” — RESPOND to the request:", ["Sure, I'd be happy to.", "I doubt it.", "That's really disappointing.", "I'm not satisfied."]),
    mc(H, "“In my view, smartphones make life easier.” — EXPRESS AGREEMENT:", ["I couldn't agree more.", "I doubt it.", "Sorry to hear that.", "Could you clarify that?"]),
    mc(H, "Which sentence expresses the STRONGEST certainty?", ["There's absolutely no doubt that AI will grow.", "AI might grow.", "Perhaps AI will grow.", "I'm not sure AI will grow."]),
    wr(H, "Give the noun of “certain”.", ["certainty"]),
    wr(H, "Give the opposite of “certainty”.", ["uncertainty"]),
    wr(H, "Give the adjective of “doubt” (full of doubt).", ["doubtful"]),
    tf(H, "“I'm positive” means “I'm sure”.", True),
]

UNIT = unit(9, "Science & Technology",
            ("Science & technology", VOCAB, V),
            ("Wishes & conditional type 3", GRAMMAR, G),
            ("Certainty & uncertainty", FUNCTIONS, F))
