from build_units import *

VOCAB = """
# Brain drain
**Brain drain** (loss of human capital) is when **skilled professionals** — doctors, engineers, scientists, academics and other **highly educated** people — leave their **country of origin** (often an **underdeveloped / developing** country) to work in a developed **host country**. They seek better opportunities, good **working conditions** or **higher salaries**.
Consequently, the countries of origin **lose** talented people, while host countries **gain** skilled workers.

# Push factors (bad conditions that force people to emigrate)
• low wages and incomes · bad living conditions · bad working conditions
• lack of advanced technology and research facilities
• bad quality of the educational system · wars and conflicts

# Pull factors (good conditions that attract people)
• higher wages · advanced technology and research facilities
• better working conditions · political stability
• intellectual freedom · a promising future · better quality of life

# Useful words
• **to emigrate** (leave your country) / **to immigrate** (come into a new country)
• **emigrant / immigrant / migration** · **skilled professionals** · **human capital**
• **brain gain** (for the host country) · **to retain talent** (keep skilled people)
"""

V = [
    mc(E, "When highly skilled people leave their country to work abroad, it's called…", ["brain drain", "brain storm", "generation gap", "culture shock"], img="✈️"),
    mc(E, "The country that receives skilled workers is the…", ["host country", "country of origin", "underdeveloped country", "home town"]),
    mc(E, "The country people come from is their country of…", ["origin", "host", "destination", "residence"], img="🏠"),
    mc(E, "Which is a PUSH factor?", ["low wages", "higher wages", "political stability", "intellectual freedom"]),
    mc(E, "Which is a PULL factor?", ["better working conditions", "wars and conflicts", "bad living conditions", "low incomes"]),
    mc(E, "To leave your country to live in another one is to…", ["emigrate", "immigrate", "retain", "invest"]),
    mc(E, "Doctors, engineers and scientists are…", ["skilled professionals", "unskilled workers", "refugees", "tourists"], img="👩‍⚕️"),
    mc(E, "Higher wages means…", ["better salaries", "lower salaries", "more taxes", "more working hours"], img="💶"),
    mc(E, "Wars and conflicts are a ___ factor.", ["push", "pull", "positive", "helping"], img="⚔️"),
    mc(E, "Political stability is a ___ factor.", ["pull", "push", "negative", "constraining"]),
    tf(E, "In brain drain, the country of origin loses talented people.", True),
    tf(E, "“Intellectual freedom” is a push factor.", False, "It attracts people, so it is a pull factor."),

    mc(M, "Brain drain is when skilled professionals such as doctors and ___ leave their country.", ["engineers", "farmers", "children", "tourists"]),
    mc(M, "They leave their country of origin, often an ___ country.", ["underdeveloped", "host", "developed", "rich"]),
    mc(M, "Their objective is to seek better opportunities, good working ___.", ["conditions", "conflicts", "taxes", "wars"]),
    mc(M, "Consequently, the countries of origin ___ talented people, while host countries gain skilled workers.", ["lose", "gain", "win", "attract"]),
    mc(M, "Many skilled professionals leave because of the promise of ___ and advanced technology abroad.", ["higher wages", "low incomes", "wars", "bad conditions"]),
    mc(M, "The chance for a ___ in a more developed environment motivates people to migrate.", ["promising future", "bad education", "low salary", "conflict"]),
    mc(M, "Gabriel says Romania has limited laboratory facilities. This is a lack of…", ["research facilities", "political stability", "higher wages", "intellectual freedom"]),
    mc(M, "People who come to live in a new country are…", ["immigrants", "emigrants", "natives", "citizens of origin"]),
    wr(M, "The opposite of “brain drain” is brain ___.", ["gain"]),
    wr(M, "The opposite of “low wages” is ___ wages.", ["higher", "high"]),
    wr(M, "Skilled people are also called highly ___ individuals. (e______)", ["educated", "skilled", "qualified"]),

    wr(H, "Give the noun of “emigrate”.", ["emigration"]),
    wr(H, "Give the noun of “migrate”.", ["migration"]),
    wr(H, "Give the adjective of “skill”.", ["skilled", "skillful", "skilful"]),
    wr(H, "Give the noun of “stable” (as in political ___).", ["stability"]),
    wr(H, "Give the person who immigrates.", ["immigrant"]),
    mc(H, "Which pair is WRONG?", ["advanced technology → push factor", "low wages → push factor", "political stability → pull factor", "wars → push factor"]),
    mc(H, "“To keep skilled workers in the country” means to ___ talent.", ["retain", "drain", "lose", "export"]),
    mc(H, "Which sentence about brain drain is correct?", ["The host country gains skilled workers.", "The host country loses skilled workers.", "The country of origin gains talented people.", "Brain drain only concerns unskilled workers."]),
    mc(H, "Which is NOT a reason for brain drain?", ["political stability at home", "low wages at home", "bad working conditions at home", "lack of research facilities at home"]),
    tf(H, "“Emigrate” means to leave your country, while “immigrate” means to come into a new country.", True),
]

GRAMMAR = """
# Relative pronouns
• **who** → people (subject of the verb): A lawyer is a person **who** helps people.
• **whom** → people (object of the verb, formal): The woman **whom** he interviewed was hired.
• **which** → things, animals and ideas: I like the present **which** my sister sent.
• **that** → people or things (in defining clauses, informal)
• **whose** → possession: The boy **whose** father is a doctor had an accident.
• **where** → places: This is the town **where** the president was born.
• **when** → time: I'll never forget the day **when** I got married.

# Joining sentences
• This is Anna's father. He gave me a phone. → This is Anna's father **who** gave me a phone.
• This is the shop. I bought my glasses from this shop. → This is the shop **where** I bought my glasses.
• The woman's husband is dead. She is called a widow. → A woman **whose** husband is dead is called a widow.

# Watch out
• Don't repeat the pronoun: ✗ the photos which he took **them** → ✓ the photos which he took.
• After a preposition, use **whom** or **which**: the professor **with whom** I was talking.
"""

G = [
    mc(E, "Nick, ___ is an excellent football player, got a lot of awards.", ["who", "whose", "which"]),
    mc(E, "He likes the present ___ his sister sent him.", ["which", "who", "where"]),
    mc(E, "This is the town ___ the president was born.", ["where", "when", "who"]),
    mc(E, "The man ___ jacket is brown is a famous actor.", ["whose", "who", "which"]),
    mc(E, "This is the season ___ the birds fly towards the West.", ["when", "where", "who"]),
    mc(E, "The boss hired the woman ___ he interviewed last week.", ["whom", "whose", "where"]),
    mc(E, "Which relative pronoun is used for possession?", ["whose", "who", "which", "where"]),
    mc(E, "Which relative pronoun is used for places?", ["where", "when", "which", "whom"]),
    mc(E, "Which relative pronoun is used for things and animals?", ["which", "who", "whom", "whose"]),
    mc(E, "A lawyer is a person ___ helps people with legal problems.", ["who", "which", "where"]),
    tf(E, "“When” is used for time.", True),
    tf(E, "“Who” is used for things and animals.", False, "We use “which” for things and animals."),

    mc(M, "Frank showed me the photos ___ he took in Istanbul.", ["which", "who", "where"]),
    mc(M, "The boy ___ father is a doctor had an accident yesterday.", ["whose", "who", "whom"]),
    mc(M, "Marrakesh is a beautiful city ___ tourists enjoy the weather and food.", ["where", "which", "when"]),
    mc(M, "I will never forget the day ___ I got married to Julia.", ["when", "where", "which"]),
    mc(M, "The professor with ___ I was talking is the best in the university.", ["whom", "who", "which"]),
    mc(M, "That is Harvard Laboratory, ___ he does scientific research.", ["where", "which", "when"]),
    mc(M, "Nabil, ___ digital camera is new, can shoot excellent photos.", ["whose", "who", "which"]),
    wr(M, "Complete: The committee members like the microscope ___ I have developed.", ["which", "that"]),
    wr(M, "Complete: I don't like the car ___ he sold last week.", ["which", "that"]),
    wr(M, "Complete: I love films ___ are about history.", ["which", "that"]),

    wr(H, "Join with “who”: This is Anna's father. He gave me a phone. → This is Anna's father ___ gave me a phone.", ["who"]),
    wr(H, "Join with “where”: This is the shop. I bought my glasses from this shop. → This is the shop ___ I bought my glasses.", ["where"]),
    wr(H, "Join with “whose”: The woman's husband is dead. She is called a widow. → A woman ___ husband is dead is called a widow.", ["whose"]),
    wr(H, "Join with “when”: I will always remember that day. On that day, I bought my mom a washing machine. → I will always remember the day ___ I bought my mom a washing machine.", ["when"]),
    wr(H, "Join with “whom”: The research centre hired the biologist. They interviewed her last week. → The research centre hired the biologist ___ they interviewed last week.", ["whom", "who", "that"]),
    mc(H, "Find the mistake: “Frank showed me the photos which he took them in Istanbul.”", ["Remove “them”.", "which → who", "took → had took", "There is no mistake."]),
    mc(H, "Find the mistake: “The man who his car was stolen called the police.”", ["who his → whose", "who → which", "was stolen → stole", "There is no mistake."]),
    mc(H, "Choose the correct sentence.", ["Agadir is the city where I was born.", "Agadir is the city which I was born.", "Agadir is the city when I was born.", "Agadir is the city who I was born."]),
    mc(H, "Choose the correct sentence.", ["Scientists who leave their country contribute to brain drain.", "Scientists which leave their country contribute to brain drain.", "Scientists whose leave their country contribute to brain drain.", "Scientists where leave their country contribute to brain drain."]),
    tf(H, "“That” can replace “who” or “which” in defining relative clauses.", True),
]

FUNCTIONS = """
# Expressing regret
We express regret when we feel sorry about something that happened (or didn't happen) in the past.

# Form: wish / if only + past perfect (had + past participle)
• Reality: I emigrated to the USA, but I can't find a job. → I **wish I had stayed** in my home country.
• Reality: The government didn't accept our demands. → **If only** they **had accepted** our demands.
• Reality: I didn't apply for the scholarship. → I **wish I had applied** for the scholarship.

# Other expressions
• I regret + -ing: I **regret leaving** my country.
• I should have + pp: I **should have studied** harder.
• It's a pity (that) … / What a shame!

# Responding to someone's regret
• Don't worry, you'll have another chance. · It's not too late. · I know how you feel.
"""

F = [
    mc(E, "Which sentence expresses REGRET?", ["I wish I had studied harder.", "I'm sure I will pass.", "Could you help me?", "Congratulations!"], img="😔"),
    mc(E, "Regret about the past uses wish / if only + …", ["past perfect", "present simple", "future simple", "present continuous"]),
    mc(E, "I didn't apply for the scholarship. → I wish I ___ for it.", ["had applied", "applied", "apply"]),
    mc(E, "If only the government ___ more in technology.", ["had invested", "invests", "will invest"]),
    mc(E, "I regret ___ my country.", ["leaving", "to leave", "leave"]),
    mc(E, "I ___ have listened to my parents. Now I regret it.", ["should", "must", "will"]),
    tf(E, "“If only I had stayed at home” expresses regret.", True),
    tf(E, "“I wish I had stayed” means that I stayed.", False, "It means I did NOT stay and I regret it."),
    mc(E, "Which word can replace “I wish” to express regret?", ["If only", "Unless", "Because", "Although"]),
    mc(E, "“What a shame!” expresses…", ["regret / disappointment", "certainty", "a request", "agreement"]),

    mc(M, "John: “If only our government had invested more, maybe more people would have stayed.” John is…", ["expressing regret", "giving advice", "making a request", "expressing certainty"]),
    mc(M, "Reality: I immigrated but can't find a job. Regret:", ["I wish I had stayed in my home country.", "I wish I stay in my home country.", "I wish I will stay in my home country."]),
    mc(M, "Mehdi: “I didn't apply for the scholarship, and now I regret it.” — Best response:", ["Don't worry, you'll have another chance next year.", "Congratulations!", "With pleasure.", "I'm not satisfied."]),
    mc(M, "Hiba: “I turned down a job abroad. Now I regret it.” She could also say:", ["I wish I hadn't turned down the job.", "I wish I turn down the job.", "I'm sure I turned down the job."]),
    mc(M, "Nadia: “I moved abroad last year, but I miss my family so much.” — Best response:", ["I know how you feel. You can visit them in the holidays.", "How fantastic!", "That's exactly what I think.", "I doubt it."]),
    mc(M, "Emma: “I wish we had recognized the importance of retaining our talent sooner.” Which verb form follows “wish”?", ["past perfect", "past simple", "present perfect", "future"]),
    wr(M, "Complete: I wish I ___ (listen) to my teacher's advice.", ["had listened"]),
    wr(M, "Complete: If only we ___ (improve) our education system earlier.", ["had improved"]),
    wr(M, "Complete: I regret ___ (not / study) harder.", ["not studying"]),
    wr(M, "Complete: I should have ___ (accept) the job offer.", ["accepted"]),

    wr(H, "Express regret: The government didn't accept our demands. → If only they ___ our demands.", ["had accepted"]),
    wr(H, "Express regret: I spent all my money. → I wish I ___ all my money.", ["hadn't spent", "had not spent"]),
    wr(H, "Express regret: We didn't keep our skilled workers. → If only we ___ our skilled workers.", ["had kept"]),
    wr(H, "Express regret with “should”: I didn't save money. → I ___ money.", ["should have saved"]),
    mc(H, "Which sentence does NOT express regret?", ["I'm sure the economy will improve.", "I wish I had stayed.", "If only I had known.", "I regret saying that."]),
    mc(H, "Which sentence is grammatically WRONG?", ["I wish I have studied abroad.", "I wish I had studied abroad.", "If only I had studied abroad.", "I regret not studying abroad."]),
    mc(H, "Aya: “Many of my best friends have left the country.” — Best response:", ["What a pity! I hope they come back one day.", "Congratulations!", "I'm positive.", "Could you lend me your notes?"]),
    mc(H, "“I wish I had a car” and “I wish I had bought a car” are different because…", ["the first is about the present, the second about the past", "the first is about the past, the second about the present", "they are both about the future", "they mean exactly the same"]),
    wr(H, "Give the noun of “regret”.", ["regret"]),
    tf(H, "“I regret leaving” and “I wish I hadn't left” have a similar meaning.", True),
]

UNIT = unit(10, "Brain Drain",
            ("Loss of human capital", VOCAB, V),
            ("Relative pronouns", GRAMMAR, G),
            ("Expressing regret", FUNCTIONS, F))
