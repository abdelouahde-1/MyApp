from build_units import *

VOCAB = """
# The situation of women
**In the past**, women were often:
• **illiterate** (illiteracy): could not read or write
• **silenced** (silence): not allowed to express opinions freely
• **dependent** (dependence): relying on others (men) for support
• **marginalized** (marginalization): pushed aside from society and opportunities
• had **limited career options**: could choose from very few jobs

**Today**, women are more:
• **literate** (literacy): able to read and write
• **outspoken**: freely expressing thoughts and opinions
• **independent** (independence): able to live without needing others' help
• **involved** (involvement): participating actively in society
• have **more career options**: can choose from many jobs

# Constraining obstacles
• **domestic violence**: physical or psychological abuse at home
• **male domination**: men controlling women unfairly
• **early age marriage**: marriage before adulthood
• **polygamy**: being married to more than one spouse at the same time
• **stereotypes**: fixed, often unfair ideas about a group of people

# Helping factors
• **gender equality**: equal rights and opportunities for men and women
• **the family code (Moudawana)**: laws that protect family members' rights
• **the media**: TV, newspapers, internet
• **education**
• **women's empowerment** (to empower): giving women power, confidence and the ability to make choices
"""

V = [
    mc(E, "A woman who cannot read or write is…", ["illiterate", "literate", "outspoken", "involved"]),
    mc(E, "A woman who freely expresses her opinions is…", ["outspoken", "silenced", "dependent", "marginalized"], img="📣"),
    mc(E, "A woman who lives without needing others' help is…", ["independent", "dependent", "marginalized", "silenced"], img="💪"),
    mc(E, "Being married to more than one spouse at the same time is…", ["polygamy", "equality", "empowerment", "literacy"]),
    mc(E, "Marriage at a very young age, before adulthood, is called…", ["early age marriage", "gender equality", "the family code", "involvement"], img="💍"),
    mc(E, "Physical or emotional abuse at home is called…", ["domestic violence", "the media", "education", "empowerment"]),
    mc(E, "Treating men and women fairly with equal rights is…", ["gender equality", "male domination", "polygamy", "stereotypes"], img="⚖️"),
    mc(E, "TV, newspapers and the internet are called…", ["the media", "the family code", "stereotypes", "career options"], img="📰"),
    mc(E, "Which is a HELPING FACTOR for women?", ["education", "early age marriage", "domestic violence", "male domination"], img="🎓"),
    mc(E, "Which is a CONSTRAINING OBSTACLE for women?", ["stereotypes", "the family code", "gender equality", "education"]),
    tf(E, "In the past, women were often marginalized and had limited career options.", True),
    tf(E, "“Dependent” means able to live without help from others.", False, "That is “independent”."),

    mc(M, "In history, women were financially ___ on men.", ["dependent", "independent", "outspoken", "involved"], "dependent on someone"),
    mc(M, "Today, women have more ___ and can choose jobs according to their interests.", ["career options", "polygamy", "stereotypes", "violence"]),
    mc(M, "Many women are now ___ in social, political and professional activities.", ["involved", "silenced", "illiterate", "dependent"]),
    mc(M, "They were often ___ from important social and economic decisions.", ["excluded", "involved", "empowered", "outspoken"]),
    mc(M, "___ can stop girls from continuing their education.", ["Early age marriage", "Gender equality", "Education", "Empowerment"]),
    mc(M, "Access to ___ allows women to develop skills and knowledge.", ["education", "polygamy", "violence", "domination"]),
    mc(M, "The ___ protects children's and women's rights within the family.", ["family code", "male domination", "stereotype", "polygamy"]),
    mc(M, "Women were silenced, but now they have become more ___.", ["outspoken", "outspeaking", "silence", "dependent"]),
    mc(M, "Too many women in developing countries suffer from ___.", ["marginalization", "marginalized", "marginalize", "marginal"]),
    wr(M, "Laws promoting gender ___ help women succeed. (equal)", ["equality"]),
    wr(M, "We need to ___ women because they have great potential. (empowerment)", ["empower"]),
    wr(M, "Many women face domestic ___ in some families. (violent)", ["violence"]),

    wr(H, "Give the noun of “independent”.", ["independence"]),
    wr(H, "Give the noun of “involved”.", ["involvement"]),
    wr(H, "Give the adjective of “equality”.", ["equal"]),
    wr(H, "Give the noun of “illiterate”.", ["illiteracy"]),
    wr(H, "In some countries, women suffered from ___ and couldn't speak freely. (silenced)", ["silence"]),
    wr(H, "Nowadays, it's difficult to defend your rights if you are ___. (illiteracy)", ["illiterate"]),
    wr(H, "Education and support programmes can lead to women's ___ in society. (empower)", ["empowerment"]),
    mc(H, "“Fixed, often unfair ideas about a group of people” are…", ["stereotypes", "career options", "the media", "laws"]),
    mc(H, "Which word is the odd one out?", ["empowerment", "polygamy", "domestic violence", "male domination"], "Empowerment is a helping factor; the others are obstacles."),
    mc(H, "Which pair shows a PAST → PRESENT change for women?", ["dependent → independent", "literate → illiterate", "outspoken → silenced", "involved → marginalized"]),
]

GRAMMAR = """
# The passive voice
We use the passive when **the action is more important than the doer**, or when **the doer is unknown or obvious**.
• Active: Feminists defend gender equality. → Passive: Gender equality **is defended** (by feminists).

# Form: object + be (in the same tense) + past participle (+ by agent)
Four steps: 1) start with the object · 2) put **be** in the tense of the active verb · 3) add the **past participle** · 4) add “by + agent” only if it is important.

# Tenses
• Present simple: They make cars in Japan → Cars **are made** in Japan.
• Present continuous: They are sending messages → Messages **are being sent**.
• Past simple: Someone stole my phone → My phone **was stolen**.
• Past continuous: He was painting my house → My house **was being painted**.
• Present perfect: Susan has fed her pets → The pets **have been fed**.
• Past perfect: Someone had stolen Kate's car → Kate's car **had been stolen**.
• Future simple: They will take the jewels → The jewels **will be taken**.
• Future perfect: We will have finished the report → The report **will have been finished**.
• Modals: They must finish the report → The report **must be finished**.

# Verbs with two objects
We sent a message to Mark → **A message was sent** to Mark / **Mark was sent** a message.
"""

G = [
    mc(E, "Gender equality ___ everywhere by feminists.", ["is defended", "defends", "is defending"]),
    mc(E, "The Pyramids ___ by the Egyptians.", ["were built", "built", "are building"]),
    mc(E, "English ___ by over 400 million people.", ["is spoken", "speaks", "is speaking"]),
    mc(E, "My phone ___ yesterday.", ["was stolen", "stole", "is stealing"]),
    mc(E, "These cars ___ in Japan.", ["are made", "make", "made"]),
    mc(E, "The passive is formed with…", ["be + past participle", "have + past participle", "be + -ing", "do + infinitive"]),
    mc(E, "The jewels ___ tomorrow.", ["will be taken", "will take", "will taken"]),
    mc(E, "The report must ___ today.", ["be finished", "finished", "finishing"]),
    mc(E, "Dinner ___ by my brother last night.", ["was cooked", "cooked", "is cook"]),
    tf(E, "We use the passive when the action is more important than the person who does it.", True),
    tf(E, "In “Women are marginalized in workplaces”, the verb is in the active voice.", False, "are + marginalized = passive."),

    mc(M, "Messages ___ to the government by many feminists right now.", ["are being sent", "are sent being", "is sending"], "present continuous passive"),
    mc(M, "My house ___ when you called me.", ["was being painted", "was painting", "has been painted"], "past continuous passive"),
    mc(M, "Susan's pets ___.", ["have been fed", "have fed", "has been feeding"], "present perfect passive"),
    mc(M, "Kate's new car ___ before she reported it.", ["had been stolen", "had stolen", "was stealing"], "past perfect passive"),
    mc(M, "This report ___ by 6:30 PM.", ["will have been finished", "will have finished", "will be finishing"], "future perfect passive"),
    mc(M, "Single mothers ___ by Aicha Chenna for a long time.", ["were defended", "defended", "are defending"]),
    wr(M, "Active: The teacher was punishing the students. → Passive: The students ___ by the teacher.", ["were being punished"]),
    wr(M, "Active: A fisherman saved his life. → Passive: His life ___ by a fisherman.", ["was saved"]),
    wr(M, "Active: Edison had invented the electric bulb. → Passive: The electric bulb ___ by Edison.", ["had been invented"]),
    wr(M, "Active: Russia is attacking Ukraine. → Passive: Ukraine ___ by Russia.", ["is being attacked"]),

    wr(H, "Active: Sarah will teach her mother how to use YouTube. → Passive: Sarah's mother ___ how to use YouTube.", ["will be taught"]),
    wr(H, "Active: Helen and Susan will have visited all these places. → Passive: All these places ___.", ["will have been visited", "will have been visited by helen and susan"]),
    wr(H, "Active: The government had offered job opportunities to disabled people. → Passive: Job opportunities ___ to disabled people.", ["had been offered"]),
    wr(H, "Active: We sent a message to Mark yesterday. → Passive: Mark ___ a message yesterday.", ["was sent"]),
    wr(H, "Active: Some people asked George about his personal life. → Passive: George ___ about his personal life.", ["was asked"]),
    wr(H, "Active: You should respect women's rights. → Passive: Women's rights ___.", ["should be respected"]),
    mc(H, "Choose the correct passive: “People speak Amazigh in many regions.”", ["Amazigh is spoken in many regions.", "Amazigh is speaking in many regions.", "Amazigh spoken in many regions.", "Amazigh was speak in many regions."]),
    mc(H, "Find the mistake: “The new law has being approved by the parliament.”", ["has being → has been", "approved → approve", "by → from", "There is no mistake."]),
    mc(H, "Why is “by someone” left out in “My phone had been stolen”?", ["Because the doer is unknown.", "Because it is a question.", "Because the verb is irregular.", "Because it is the future."]),
    tf(H, "“The presentations must be delivered tomorrow” is the passive of “Students must deliver the presentations tomorrow.”", True),
]

FUNCTIONS = """
# Linking words of ADDITION (to add an idea)
• **and / also / too / as well as**: Women are marginalized **and** dependent.
• **In addition, / Moreover, / Furthermore, / Additionally, / Besides, / In addition to this,** + new sentence:
In the past, women were silenced. **Moreover**, they were marginalized.
• **in addition to / along with / besides + noun or gerund**: **In addition to** being silenced, women were marginalized.
• **not only … but also …**: Women are **not only** outspoken **but also** involved.
With other verbs: Women **not only** faced discrimination **but** they **also** had no access to education.

# Linking words of CONCESSION / CONTRAST (to show an unexpected idea)
• **but / yet**: The situation is bad, **but** many associations are working to improve it.
• **However, / Nevertheless,** + new sentence: Globalization helped women. **However**, they are still seen as inferior in some societies.
• **whereas / while**: contrast between two ideas in one sentence.
• **although / though / even though + clause**: **Although** women were marginalized, they contributed to the economy.
• **despite / in spite of + noun or gerund**: **Despite** facing discrimination, many women build great careers. / **In spite of** the difficulties, …
• **despite the fact that + clause**
"""

F = [
    mc(E, "Which linking word expresses ADDITION?", ["moreover", "however", "although", "despite"]),
    mc(E, "Which linking word expresses CONCESSION (contrast)?", ["although", "furthermore", "in addition", "also"]),
    mc(E, "In the past, women were silenced. ___, they were marginalized.", ["Moreover", "However", "Although"]),
    mc(E, "Women have outstanding capabilities. ___, many can't find a job.", ["However", "Furthermore", "In addition"]),
    mc(E, "Females have potential ___ courage.", ["and", "but", "although"]),
    mc(E, "___ women were marginalized, they contributed a lot to the economy.", ["Although", "Despite", "Moreover"]),
    mc(E, "___ the difficulties, many girls succeed at school.", ["Despite", "Although", "Furthermore"], "despite + noun"),
    mc(E, "Women are not only outspoken ___ involved.", ["but also", "and also", "however"]),
    tf(E, "“Furthermore” has the same function as “in addition”.", True),
    tf(E, "“In spite of” is followed by a full clause (subject + verb).", False, "In spite of + noun / gerund."),
    mc(E, "The situation of women is still bad, ___ many associations are working to improve it.", ["but", "and", "moreover"]),
    mc(E, "Mass media helped women speak out. It ___ highlights their suffering.", ["also", "however", "although"]),

    mc(M, "Feminist writers change negative stereotypes. In addition to ___, they defend women's rights.", ["this", "that they", "it is"]),
    mc(M, "In Morocco, women still suffer from stereotypes as well as ___.", ["marginalization", "they are marginalized", "marginalize"], "as well as + noun"),
    mc(M, "___ facing discrimination at work, many women build great careers.", ["Despite", "Although", "However"], "despite + gerund"),
    mc(M, "Women are often responsible for housework, ___ they manage to pursue higher education.", ["yet", "moreover", "in addition"]),
    mc(M, "___ girls live far from school, they have proven their intelligence.", ["Even though", "In spite of", "Despite"]),
    mc(M, "Illiterate women have limited career options in addition to ___ under male domination.", ["being", "be", "are"], "in addition to + gerund"),
    mc(M, "Women must be self-confident. ___, they should know when to say “No”.", ["Moreover", "However", "Although"]),
    mc(M, "Educated women have a big influence on society. ___, they are not involved in political life.", ["However", "Furthermore", "Besides"]),
    wr(M, "Complete: In spite ___ the efforts of the media, many women still suffer.", ["of"]),
    wr(M, "Complete: Women not ___ faced discrimination but also had no access to education.", ["only"]),
    wr(M, "Complete: ___ the fact that women are equal to men, they are not given importance in some countries. (one word)", ["despite"]),

    mc(H, "Join: “Women are outspoken. They are involved and active.” (not only … but also)", ["Women are not only outspoken but also involved and active.", "Women not only are outspoken but involved also.", "Not only women are outspoken but also active they are."]),
    mc(H, "Join: “Too many girls live far from school. They have proven intelligence.” (despite)", ["Despite living far from school, too many girls have proven intelligence.", "Despite they live far from school, girls have proven intelligence.", "Despite of living far from school, girls have proven intelligence."]),
    mc(H, "Join: “Women were marginalized. They contributed a lot to the economy.” (although)", ["Although women were marginalized, they contributed a lot to the economy.", "Although women were marginalized, but they contributed a lot.", "Although of their marginalization, they contributed a lot."]),
    mc(H, "Which sentence is WRONG?", ["Despite she was tired, she continued working.", "Although she was tired, she continued working.", "Despite being tired, she continued working.", "In spite of her tiredness, she continued working."]),
    mc(H, "Which sentence is WRONG?", ["In addition to she is clever, she is hard-working.", "In addition to being clever, she is hard-working.", "She is clever. In addition, she is hard-working.", "She is not only clever but also hard-working."]),
    wr(H, "Rewrite with “despite”: Although she was poor, she studied hard. → Despite ___ poor, she studied hard.", ["being"]),
    wr(H, "Rewrite with “in spite of”: Although it rained, they played. → In spite of the ___, they played.", ["rain"]),
    wr(H, "Rewrite with “although”: Despite her illness, she went to work. → Although she ___ ill, she went to work.", ["was"]),
    mc(H, "Which linking word can REPLACE “Nevertheless” in: “She was tired. Nevertheless, she finished her work.”?", ["However", "Moreover", "Besides", "Therefore"]),
    mc(H, "Which linking word can REPLACE “Furthermore” in: “Girls need education. Furthermore, they need self-confidence.”?", ["Additionally", "However", "Although", "Thus"]),
    tf(H, "“Whereas” is used to contrast two ideas in the same sentence.", True),
]

UNIT = unit(5, "Women and Power",
            ("Situation of women, obstacles & factors", VOCAB, V),
            ("The passive voice", GRAMMAR, G),
            ("Linking words of addition & concession", FUNCTIONS, F))
