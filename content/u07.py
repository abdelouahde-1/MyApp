from build_units import *

VOCAB = """
# Citizenship
• **citizenship**: being a member of a country, with **rights** and **duties**
• **rights**: what citizens are entitled to have
• **duties**: what citizens must do

# Rights
• to **vote** · to **have a say** (to express your opinion in decisions)
• to have **access to education** · to have **access to healthcare**
• to **be safe** · to **have residency** · freedom of speech

# Duties
• to **pay taxes** · to **obey the law**
• to **respect the environment** · to **respect the national flag / anthem**
• to **do voluntary work** · to be concerned with **the common good**

# Good vs bad citizens
• Good citizens **are** active and responsible, **obey** the law, **respect** other citizens, **do** voluntary work, **participate** in the democratic process, **protect** the environment.
• Bad citizens **are not** engaged, **break** the law, **harm** other citizens, **do not take part** in community service, **vandalize** public property.

# Useful words
• **active citizen** · **community service** · **charity / charitable institutions** · **national anthem** · **the common good**: actions that benefit the whole community
"""

V = [
    mc(E, "Choosing a representative in elections is to…", ["vote", "pay taxes", "vandalize", "obey"], img="🗳️"),
    mc(E, "Money citizens pay to the government is called…", ["taxes", "salaries", "scholarships", "rights"], img="💰"),
    mc(E, "Working for free to help others is…", ["voluntary work", "taxes", "residency", "vandalism"], img="🙋"),
    mc(E, "Which is a RIGHT?", ["to have access to education", "to pay taxes", "to obey the law", "to respect the flag"], img="🏫"),
    mc(E, "Which is a DUTY?", ["to obey the law", "to be safe", "to have access to healthcare", "to have a say"], img="⚖️"),
    mc(E, "The song of a country is its national…", ["anthem", "flag", "tax", "law"], img="🎵"),
    mc(E, "Damaging public property on purpose is to…", ["vandalize", "protect", "participate", "vote"]),
    mc(E, "Good citizens ___ the environment.", ["protect", "harm", "vandalize", "break"], img="🌳"),
    mc(E, "Bad citizens ___ the law.", ["break", "obey", "respect", "protect"]),
    mc(E, "Having access to hospitals and doctors is the right to…", ["healthcare", "residency", "taxes", "vote"], img="🏥"),
    tf(E, "Paying taxes is a duty of citizens.", True),

    mc(M, "Citizens must pay ___ so that the government can finance health, education and infrastructure.", ["taxes", "anthems", "rights", "votes"]),
    mc(M, "Helping old people in hospitals or visiting orphanages is part of community ___.", ["service", "tax", "law", "anthem"]),
    mc(M, "Kamal is an ___. He always helps people in need.", ["active citizen", "illiterate person", "obstinate person", "selfish person"]),
    mc(M, "Whenever the King makes a speech, the ___ is sung before and after it.", ["national anthem", "common good", "voluntary work", "residency"]),
    mc(M, "All good citizens obey ___ so as not to turn society into a jungle.", ["the law", "the taxes", "the anthem", "the vandals"]),
    mc(M, "___ means acting in ways that benefit the entire community.", ["The common good", "Vandalism", "Residency", "Individuality"]),
    mc(M, "Good citizens ___ in the democratic process.", ["participate", "vandalize", "harm", "break"]),
    mc(M, "Bad citizens do not take ___ in community service.", ["part", "place", "care", "time"]),
    mc(M, "To express your opinion in decisions that affect you is to…", ["have a say", "have residency", "pay taxes", "obey"]),
    wr(M, "The opposite of “obey the law” is ___ the law.", ["break", "disobey", "violate"]),
    wr(M, "He always does ___ work for elders in charitable institutions. (unpaid)", ["voluntary", "volunteer"]),

    wr(H, "Give the noun of “participate”.", ["participation"]),
    wr(H, "Give the noun of “vote” (the person who votes).", ["voter"]),
    wr(H, "Give the adjective of “responsibility”.", ["responsible"]),
    wr(H, "Give the noun of “citizen” (the status).", ["citizenship"]),
    wr(H, "Give the noun of “vandalize”.", ["vandalism"]),
    mc(H, "Which is NOT a duty of a citizen?", ["to have access to healthcare", "to pay taxes", "to obey the law", "to respect the environment"]),
    mc(H, "Which is NOT a right of a citizen?", ["to respect the national anthem", "to vote", "to be safe", "to have access to education"]),
    mc(H, "Which action describes a BAD citizen?", ["writing on public walls", "voting in elections", "cleaning the neighbourhood", "helping in an orphanage"]),
    mc(H, "“Citizenship” is best defined as…", ["being a member of a country with rights and duties", "living in a city", "paying taxes only", "travelling abroad"]),
    tf(H, "Being concerned with “the common good” means thinking only of your own interests.", False),
]

GRAMMAR = """
# Reported speech: statements
Reporting verbs: **said (that)**, **told + person (that)**, explained, answered, informed …
When the reporting verb is in the past, the tense goes **one step back**:
• present simple → **past simple**: “I am tired.” → He said (that) he **was** tired.
• present continuous → **past continuous**: “I am working.” → She said she **was working**.
• past simple → **past perfect**: “We finished.” → They said they **had finished**.
• present perfect → **past perfect**: “I have sent it.” → He said he **had sent** it.
• past perfect → **past perfect** (no change)
• am/is/are going to → **was/were going to**

# Commands, requests and advice
• Affirmative: reporting verb + **object + to + infinitive**: “Do your homework!” → My mother told me **to do** my homework.
• Negative: reporting verb + **object + not to + infinitive**: “Don't fight!” → She warned them **not to fight**.
• Reporting verbs: told, ordered, asked, advised, warned, reminded, urged, begged.
• Advice: “You should rest.” → He advised me **to rest**.

# Modals
• will → **would** · can → **could** · may → **might** · must / have to → **had to**

# Time and place
• today → **that day** · now → **then** · yesterday → **the day before / the previous day**
• tomorrow → **the next day / the following day** · next week → **the following week**
• last Monday → **the previous Monday** · ago → **before**
• this → **that** · these → **those** · here → **there**

# Pronouns
Change pronouns to match the speaker: “I love my job.” → She said **she** loved **her** job.
"""

G = [
    mc(E, "“I am tired.” He said that he ___ tired.", ["was", "is", "has been"]),
    mc(E, "“We have a meeting.” She said that they ___ a meeting.", ["had", "have", "has"]),
    mc(E, "“I am working.” She said she ___.", ["was working", "is working", "works"]),
    mc(E, "“Do your homework!” My mother told me ___ my homework.", ["to do", "do", "doing"]),
    mc(E, "“Don't fight at home!” The mother warned them ___ at home.", ["not to fight", "to not fighting", "don't fight"]),
    mc(E, "In reported speech, “will” becomes…", ["would", "will", "could"]),
    mc(E, "In reported speech, “can” becomes…", ["could", "can", "would"]),
    mc(E, "In reported speech, “tomorrow” becomes…", ["the next day", "yesterday", "today"]),
    mc(E, "In reported speech, “here” becomes…", ["there", "here", "this"]),
    mc(E, "In reported speech, “now” becomes…", ["then", "now", "today"]),
    tf(E, "In reported speech, “must” usually becomes “had to”.", True),
    tf(E, "In reported speech, “yesterday” stays “yesterday”.", False, "It becomes “the day before”."),

    mc(M, "“I want to vote for the Liberal Party.” Luke told me that he ___ to vote for the Liberal Party.", ["wanted", "wants", "had wanted to"]),
    mc(M, "“Samira wrote an excellent poem.” Nabil said that Samira ___ an excellent poem.", ["had written", "has written", "writes"]),
    mc(M, "“We haven't painted the room.” They told their boss that they ___ the room.", ["hadn't painted", "haven't painted", "didn't paint"]),
    mc(M, "“She is going to help us.” Ahlam said that she ___ help them.", ["was going to", "is going to", "will"]),
    mc(M, "“You should stop smoking!” The doctor advised me ___ smoking.", ["to stop", "stop", "stopping"]),
    mc(M, "“Help me, please.” He asked Sofia ___ him.", ["to help", "help", "helps"]),
    mc(M, "“Don't leave your room.” He asked me ___ my room.", ["not to leave", "don't leave", "not leaving"]),
    mc(M, "“I will try to visit you after work.” He told me that he ___ try to visit me after work.", ["would", "will", "could"]),
    mc(M, "“Take your umbrella because it may rain.” My mom advised me to take my umbrella because it ___ rain.", ["might", "may", "will"]),
    wr(M, "“I have an important meeting today.” → Alex said that he had an important meeting ___.", ["that day"]),
    wr(M, "“I didn't finish the work yesterday.” → The worker said that he hadn't finished the work ___.", ["the day before", "the previous day"]),
    wr(M, "“Never touch these headphones.” → Khadija reminded me never to touch ___ headphones.", ["those"]),

    wr(H, "Report: “I am cleaning my neighbourhood.” said Achraf. → Achraf said (that) he ___ his neighbourhood.", ["was cleaning"]),
    wr(H, "Report: “Tim had broken your vase!” Linda told Sam. → Linda told Sam that Tim ___ his vase.", ["had broken"]),
    wr(H, "Report: “My father has cooked dinner,” I told my mother. → I told my mother that my father ___ dinner.", ["had cooked"]),
    wr(H, "Report: “Get out of my car!” she told him. → She told him ___ of her car.", ["to get out"]),
    wr(H, "Report: “Don't travel alone.” They warned me. → They warned me ___ alone.", ["not to travel"]),
    wr(H, "Report: “Children, you must finish your work.” → The teacher ordered the children ___ their work.", ["to finish"]),
    wr(H, "Report: “The children have to sleep early.” → The father said that the children ___ sleep early.", ["had to"]),
    wr(H, "Report: “We will climb this mountain tomorrow.” → Luke told us that they would climb ___ mountain the next day.", ["that"]),
    mc(H, "Report: “My family and I are going to start our trip next week,” said Bob.", ["Bob said that he and his family were going to start their trip the following week.", "Bob said that my family and I are going to start our trip next week.", "Bob said that he and his family will start their trip next week."]),
    mc(H, "Report: “I have lived here for sixteen years,” my neighbour told us.", ["My neighbour told us that he had lived there for sixteen years.", "My neighbour told us that he has lived here for sixteen years.", "My neighbour told us that I had lived there for sixteen years."]),
    mc(H, "Find the mistake: “She told me that she will come the next day.”", ["will → would", "told → said", "the next day → tomorrow", "There is no mistake."]),
]

FUNCTIONS = """
# Asking for advice
• What should I do?
• What do you advise me to do?
• What would you do in this situation?
• If you were me / in my place, what would you do?
• Can you give me some advice?

# Giving advice
• You should / shouldn't + verb …
• I think you should …
• You ought to + verb …
• You'd (had) better + verb … / You'd better not …
• I advise you to + verb …
• If I were you, I would …
• Why don't you …?
• Whatever you do, don't …

# Remember
• **advice** (noun, uncountable): a piece of advice · **advise** (verb): I advise you to …
• should / ought to / had better are followed by the **base form**.
"""

F = [
    mc(E, "Which expression ASKS for advice?", ["What should I do?", "You should…", "I advise you to…", "If I were you…"]),
    mc(E, "Which expression GIVES advice?", ["If I were you, I would…", "What would you do?", "What do you advise me to do?", "Can you give me some advice?"]),
    mc(E, "You ___ see a doctor. You look sick.", ["should", "shouldn't", "would"], img="🤒"),
    mc(E, "I advise you ___ these new words up.", ["to look", "looking", "look"]),
    mc(E, "If I ___ you, I would study harder.", ["were", "am", "was being"]),
    mc(E, "You'd better ___ early tonight.", ["sleep", "to sleep", "sleeping"]),
    mc(E, "You ought ___ your teacher.", ["to tell", "tell", "telling"]),
    tf(E, "“What do you advise me to do?” is a way of giving advice.", False, "It asks for advice."),
    tf(E, "“Whatever you do, don't cheat” is a piece of advice.", True),
    mc(E, "Can you give me a piece of ___?", ["advice", "advise", "advices"]),

    mc(M, "“I don't understand some words in this book. What should I do?” — Best answer:", ["I advise you to look them up.", "I don't share the same view.", "You're right. Reading is good."]),
    mc(M, "“I want to be fluent in English. If you were in my place, what would you do?” — Best answer:", ["I would watch English films and practise every day.", "I'm sorry for this mistake.", "With pleasure."]),
    mc(M, "“___” — “You should practise sport if you want to be fit.”", ["What should I do to be fit?", "Could you clarify that?", "I'm not satisfied.", "Why are you late?"]),
    mc(M, "“I have a lot of work. Can you give me a hand?” — Best answer:", ["Sure, I'd be happy to help.", "I agree with you. Working is important.", "I am sorry for this mistake."]),
    mc(M, "“I'm not sure I get your point. What is ‘forthright’?” — Best answer:", ["It simply means direct and honest.", "In my opinion, it's a perfect choice.", "I am sorry, I have no time."]),
    mc(M, "Adam got a message asking him to cheat. Lucy says: “Whatever you do, don't cheat.” Lucy is…", ["giving advice", "asking for advice", "complaining", "apologizing"]),
    wr(M, "Complete: If I ___ you, I would tell the teacher.", ["were"]),
    wr(M, "Complete: You'd ___ not use your phone in class. (one word)", ["better"]),
    wr(M, "Complete: What would you do in this ___?", ["situation", "case"]),
    wr(M, "Complete: You ___ to apologize to her. (one word: o____)", ["ought"]),

    mc(H, "Which sentence is grammatically WRONG?", ["You should to rest.", "You should rest.", "You ought to rest.", "You'd better rest."]),
    mc(H, "Which sentence is grammatically WRONG?", ["I advise you studying more.", "I advise you to study more.", "If I were you, I'd study more.", "Why don't you study more?"]),
    mc(H, "Your friend always feels tired. The best advice is:", ["If I were you, I would sleep earlier.", "If you were me, what would you do?", "Could you repeat, please?", "I'm awfully sorry."]),
    mc(H, "“If you were in my shoes, what would you do?” The function is…", ["asking for advice", "giving advice", "making a complaint", "expressing purpose"]),
    wr(H, "Give the verb of “advice”.", ["advise"]),
    wr(H, "Complete with a verb: Why don't you ___ (join) a volunteer club?", ["join"]),
    wr(H, "Complete: I think you ___ (should) apologize. → Rewrite with “had better”: You ___ apologize.", ["had better", "'d better", "you'd better"]),
    mc(H, "Which reply does NOT give advice?", ["What should I do?", "You should talk to your parents.", "I advise you to rest.", "If I were you, I'd say no."]),
    mc(H, "Choose the most appropriate advice for a student who wants to be an active citizen.", ["You should do voluntary work in your neighbourhood.", "You should vandalize public property.", "You shouldn't respect the law.", "You'd better not vote."]),
    tf(H, "“You'd better not” is followed by the base form: You'd better not be late.", True),
]

UNIT = unit(7, "Citizenship",
            ("Active citizenship: rights & duties", VOCAB, V),
            ("Reported speech", GRAMMAR, G),
            ("Asking for & giving advice", FUNCTIONS, F))
