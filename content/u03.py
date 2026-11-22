from build_units import *

VOCAB = """
# Key words
• **education**: the acquisition of knowledge
• **to graduate**: to finish school or university
• **certificate / degree / diploma**: what students get when they finish their studies
• **scholarship**: money given to students to help pay for their studies
• **abroad**: outside your country
• **school fees**: money you pay to study
• **boarding school**: a school where students who live far away study and live
• **higher education**: studying at college, university or higher institutes
• **e-learning**: remote learning using computers and the internet

# School levels
• pre-school / kindergarten → primary school → middle school → secondary (high) school → higher education (university)

# Types of education
• **Formal education**: in school; sponsored by the government; obligatory (compulsory); ends with a degree. Examples: primary school, secondary school, university, college.
• **Non-formal education**: programmes in a school-like setting; not obligatory; usually not evaluated; no degree; often sponsored by associations. Examples: literacy programmes, music classes, extra hours.
• **Informal education**: unintentional; happens in daily life and work; lifelong. Examples: learning from parents, reading books, watching documentaries.

# Literacy
• **literacy**: the ability to read and write → **literate** person
• **illiteracy**: the inability to read and write → **illiterate** person
• **literacy classes / programmes**: classes that teach adults to read and write
"""

V = [
    mc(E, "Money given to students to help them pay for their studies is a…", ["scholarship", "certificate", "degree", "fee"], img="💵"),
    mc(E, "“Outside the country” means…", ["abroad", "boarding", "inside", "formal"], img="✈️"),
    mc(E, "To finish school or university is to…", ["graduate", "enrol", "fail", "apply"], img="🎓"),
    mc(E, "The ability to read and write is called…", ["literacy", "illiteracy", "e-learning", "graduation"], img="📖"),
    mc(E, "Learning remotely with computers and the internet is…", ["e-learning", "boarding school", "formal education", "literacy"], img="💻"),
    mc(E, "A school where students live and study is a…", ["boarding school", "primary school", "kindergarten", "university"], img="🏫"),
    mc(E, "Which type of education ends with a degree?", ["formal education", "informal education", "non-formal education"]),
    mc(E, "Learning from your parents at home is an example of…", ["informal education", "formal education", "higher education"], img="👨‍👩‍👧"),
    mc(E, "Literacy programmes and music classes are examples of…", ["non-formal education", "formal education", "informal education"], img="🎻"),
    mc(E, "Studying at university is called…", ["higher education", "primary education", "illiteracy", "kindergarten"]),
    tf(E, "Formal education is usually sponsored by the government and is obligatory.", True),
    tf(E, "Informal education always ends with a certificate.", False, "Informal education is unintentional and leads to no degree."),

    mc(M, "Next year, Amal will ___ from high school and get her certificate.", ["graduate", "abroad", "attend", "study"]),
    mc(M, "She plans to go ___ to finish her higher education.", ["abroad", "aboard", "away from", "outside of"]),
    mc(M, "Because she can't afford the school fees, she has applied for a ___.", ["scholarship", "certificate", "literacy", "boarding"]),
    mc(M, "During Covid-19, face-to-face learning temporarily changed into ___.", ["e-learning", "boarding school", "literacy", "informal education"]),
    mc(M, "Formal education starts at primary school and goes on to ___.", ["higher education", "kindergarten", "literacy classes", "boarding"]),
    mc(M, "My uncle attends literacy classes because he is an ___ person.", ["illiterate", "literate", "educated", "graduate"]),
    mc(M, "Some villages have no schools, so students stay at the ___.", ["boarding school", "university", "kindergarten", "library"]),
    mc(M, "Which is a characteristic of NON-FORMAL education?", ["It happens in a school-like setting but gives no degree.", "It is obligatory and ends with a degree.", "It is unintentional and happens in daily life."]),
    mc(M, "Watching documentaries and reading books at home are examples of ___ education.", ["informal", "formal", "non-formal", "higher"]),
    wr(M, "The opposite of “literate” is ___.", ["illiterate"]),
    wr(M, "The degree students get when they finish school is a ___. (c__________)", ["certificate"]),

    wr(H, "Give the noun of “illiterate”.", ["illiteracy"]),
    wr(H, "Give the noun of “educate”.", ["education"]),
    wr(H, "Give the noun of “graduate” (the act or ceremony of graduating).", ["graduation"]),
    wr(H, "Complete: Fatima attends ___ classes so that she can learn to read and write.", ["literacy"]),
    wr(H, "Complete: Formal education is ___ (= obligatory) for children in Morocco.", ["compulsory", "obligatory", "mandatory"]),
    mc(H, "“It is often unintentional and happens outside school, in daily life and work.” This describes…", ["informal education", "formal education", "non-formal education", "higher education"]),
    mc(H, "“It is in a school-like setting, but it is not evaluated and doesn't lead to a degree.” This describes…", ["non-formal education", "formal education", "informal education", "e-learning"]),
    mc(H, "Which word does NOT belong to formal education?", ["unintentional", "obligatory", "degree", "sponsored by the government"]),
    mc(H, "Which sentence is correct?", ["Many graduates apply for scholarships to study abroad.", "Many graduates apply for scholarships to study aboard.", "Many graduates apply for literacy to study abroad."]),
    tf(H, "“Lifelong learning” is a characteristic of informal education.", True),
]

GRAMMAR = """
# Past perfect simple
**Form:** had + past participle
• (+) I **had learnt** some Arabic before I moved to Morocco.
• (–) She **hadn't studied** Arabic before she came.
• (?) **Had** you **studied** Arabic before you moved?

# Use
We use the past perfect for the **first** of two past actions (the earlier one). The second action is in the **past simple**.
• Lucy got her certificate. Then she worked as a manager. → After Lucy **had got** her certificate, she **worked** as a manager.
• Before John **moved** to Morocco, he **had studied** Arabic.

# Time words
• **after, as soon as, already, ever, never, just** → usually go with the **past perfect** action.
• **when, before, by the time** → usually go with the **past simple** action.
• When we arrived at the station, the train **had already left**.
"""

G = [
    mc(E, "The past perfect is formed with…", ["had + past participle", "have + past participle", "was + -ing", "did + verb"]),
    mc(E, "When we arrived at the station, the train ___.", ["had already left", "already leaves", "has already left"]),
    mc(E, "After I ___ my homework, I played video games.", ["had done", "have done", "do"]),
    mc(E, "Before Mark moved to Morocco, he ___ some Arabic.", ["had learnt", "learns", "has learnt"]),
    mc(E, "She ___ in China before she went to Thailand.", ["hadn't lived", "doesn't live", "hasn't lived"]),
    mc(E, "___ you studied Arabic before you moved to Morocco?", ["Had", "Have", "Did"]),
    mc(E, "In “After I had talked to him, I felt better”, which action happened first?", ["talking to him", "feeling better", "both at the same time"]),
    mc(E, "They ___ already eaten when we arrived.", ["had", "have", "were"]),
    mc(E, "When he arrived at the party, Julie ___ already left.", ["had", "has", "was"]),
    mc(E, "Which word usually goes with the past perfect action?", ["already", "when", "tomorrow", "now"]),
    tf(E, "We use the past perfect for the earlier of two past actions.", True),
    tf(E, "“Last year, Ali had passed all his exams.” is the best sentence when there is only one past action.", False, "With a single past action we use the past simple: Ali passed all his exams."),

    mc(M, "When we ___ at the station, the train had already left.", ["arrived", "had arrived", "arrive"]),
    mc(M, "He wasn't angry because he ___ long before I entered.", ["hadn't waited", "didn't waited", "hasn't waited"]),
    mc(M, "As soon as they ___ the car, they sold it.", ["had repaired", "repair", "have repaired"]),
    mc(M, "When I arrived at the company, the director ___. He was still there.", ["hadn't left", "had left", "has left"]),
    mc(M, "I ___ my room before I went to bed.", ["had cleaned", "have cleaned", "clean"]),
    mc(M, "Beth came round after she ___ Rob to the station.", ["had taken", "has taken", "takes"]),
    wr(M, "After I ___ (talk) to him, I felt better.", ["had talked"]),
    wr(M, "They ___ (already / eat) when we arrived.", ["had already eaten"]),
    wr(M, "As soon as we had put our bags in our rooms, we ___ (have) lunch.", ["had"], "Second action → past simple"),
    wr(M, "She ___ (not / live) in China before she went to Thailand.", ["had not lived", "hadn't lived"]),

    wr(H, "Join with “after”: Lucy got her certificate. Then she worked as a manager. → After Lucy ___ her certificate, she worked as a manager.", ["had got", "had gotten"]),
    wr(H, "Join with “before”: John studied Arabic, then moved to Morocco. → Before John moved to Morocco, he ___ Arabic.", ["had studied"]),
    wr(H, "Make it a question: ___ you picked the tickets before he came to the theatre?", ["had"]),
    wr(H, "Last year, Ali ___ (pass) all his exams.", ["passed"], "Only one past action → past simple"),
    mc(H, "Choose the correct sentence.", ["By the time the police arrived, the thief had escaped.", "By the time the police had arrived, the thief escaped.", "By the time the police arrived, the thief has escaped."]),
    mc(H, "Choose the correct sentence.", ["I had never seen the sea before I visited Agadir.", "I have never seen the sea before I visited Agadir.", "I never had seen the sea before I visit Agadir."]),
    mc(H, "Find the mistake: “After she has finished her studies, she went abroad.”", ["has finished → had finished", "went → had gone", "abroad → aboard", "There is no mistake."]),
    mc(H, "“The film had started when we arrived.” This means…", ["We missed the beginning of the film.", "The film started after we arrived.", "We arrived before the film."]),
    mc(H, "“The film started when we arrived.” This means…", ["The film started at the moment we arrived.", "We missed the beginning of the film.", "The film had finished."]),
    tf(H, "In “I had cleaned my room before I went to bed”, cleaning happened before going to bed.", True),
]

FUNCTIONS = """
# Expressing purpose (why we do something)
• **to + infinitive**: She studies hard **to pass** her exams.
• **in order to / so as to + infinitive**: Yassine applied for a scholarship **in order to study** abroad.
• **in order not to / so as not to + infinitive** (negative purpose): I study every day **so as not to fail**.
• **so that + subject + can / could / will / would + verb**: Sophie attends literacy classes **so that she can read**.
• **for + noun**: Students go to school **for a diploma**.
• **for + gerund** (the purpose of a thing): A dictionary is used **for checking** words.

# Watch out
• After **in order to / so as to** use the **base form**: in order to study (NOT in order to studying).
• After **so that** use a full clause with a subject and usually a modal.
• Use **could / would** after so that when the main verb is in the past: He worked hard so that he **could** buy a car.
"""

F = [
    mc(E, "Which expression shows PURPOSE?", ["in order to", "although", "because of", "however"]),
    mc(E, "Yassine applied for a scholarship ___ study abroad.", ["in order to", "so that", "because", "for"]),
    mc(E, "Fatima attends literacy classes so ___ she can read.", ["that", "as", "to", "for"]),
    mc(E, "Students go to school ___ a diploma.", ["for", "to", "so that", "in order to"], "for + noun"),
    mc(E, "I study every day so as not ___ my exams.", ["to fail", "fail", "failing"]),
    mc(E, "He opened the door slowly ___ wake his parents.", ["so as not to", "in order to", "so that"]),
    mc(E, "I phoned her ___ see if she was fine.", ["to", "for", "so that"]),
    tf(E, "“In order to” is followed by the base form of the verb.", True),
    tf(E, "“So that” is followed directly by a verb in -ing.", False, "So that + subject + modal + verb."),
    mc(E, "“Why do you learn English?” — “___ communicate with people from other countries.”", ["To", "For", "So that"]),

    mc(M, "Sophie attends literacy classes ___ she can read.", ["so that", "in order to", "so as to"]),
    mc(M, "They work day and night ___ money.", ["to earn", "so that", "for earn"]),
    mc(M, "We ran to the station so as not to ___ the train.", ["miss", "missing", "missed"]),
    mc(M, "I turned off the music ___ I would not disturb my sister.", ["so that", "in order to", "so as not to"]),
    mc(M, "She bought a dictionary ___ improve her English.", ["to", "so that", "for"]),
    mc(M, "He saved money so that he ___ buy a car.", ["could", "can to", "to"], "Past context → could"),
    wr(M, "Complete: Janette took a taxi so as not ___ late. (be)", ["to be"]),
    wr(M, "Complete: Mr. David is saving money in ___ to buy a car.", ["order"]),
    wr(M, "Complete: He jumped into the river so as ___ rescue the drowning child.", ["to"]),
    wr(M, "Complete: Students are working hard in order not ___ fail.", ["to"]),

    mc(H, "Join: “Students are working hard. They don't want to fail.”", ["Students are working hard so as not to fail.", "Students are working hard so as to fail.", "Students are working hard so that not fail.", "Students are working hard for not failing."]),
    mc(H, "Join: “Janette took a taxi. She didn't want to be late.”", ["Janette took a taxi so that she wouldn't be late.", "Janette took a taxi so that she won't to be late.", "Janette took a taxi in order to not late.", "Janette took a taxi for not be late."]),
    mc(H, "Which sentence is WRONG?", ["She went to the library in order to studying.", "She went to the library to study.", "She went to the library so as to study.", "She went to the library so that she could study."]),
    mc(H, "Join: “Mr. David is saving money. He wants to buy a car.”", ["Mr. David is saving money so that he can buy a car.", "Mr. David is saving money so that buy a car.", "Mr. David is saving money for buy a car.", "Mr. David is saving money in order buying a car."]),
    wr(H, "Rewrite with “so that”: I turned off the music so as not to disturb my sister. → I turned off the music so that I ___ disturb my sister.", ["wouldn't", "would not", "didn't", "won't"]),
    wr(H, "Rewrite with “in order to”: She learns English so that she can travel. → She learns English in order ___ travel.", ["to"]),
    wr(H, "Complete with one word: A knife is used ___ cutting things.", ["for"]),
    mc(H, "Which expression of purpose is followed by a NOUN?", ["for", "so as to", "in order to", "so that"]),
    mc(H, "“Why do people listen to music?” — Best answer expressing purpose:", ["In order to relax.", "Because of the radio.", "Although it is loud.", "As a result, they dance."]),
    tf(H, "“I went to Rabat so as to visit my aunt” and “I went to Rabat so that I could visit my aunt” have the same meaning.", True),
]

UNIT = unit(3, "Types of Education",
            ("Education & literacy", VOCAB, V),
            ("Past perfect simple", GRAMMAR, G),
            ("Expressing purpose", FUNCTIONS, F))
