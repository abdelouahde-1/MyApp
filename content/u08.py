from build_units import *

VOCAB = """
# International organizations
**International organizations** are groups of countries (or people from many countries) working together on global issues such as peace, health, food, human rights and education.

# Organizations and their concerns
• **UN** – United Nations: fostering world peace, stopping conflicts and wars, promoting international cooperation
• **FAO** – Food and Agriculture Organization: fighting hunger, ensuring food security and good nutrition
• **WHO** – World Health Organization: health issues worldwide, fighting dangerous diseases
• **ICRC** – International Committee of the Red Cross: protecting human life and health, protecting victims of armed conflicts, helping victims of natural disasters
• **UNHCR** – United Nations High Commissioner for Refugees: protecting refugees, providing shelter, resettlement in another country
• **AI** – Amnesty International: defending human rights, freeing political prisoners
• **TI** – Transparency International: fighting political and economic corruption
• **UNICEF** – United Nations International Children's Emergency Fund: helping children and mothers in underdeveloped countries
• **UNESCO** – United Nations Educational, Scientific and Cultural Organization: education, science, culture and communication among nations

# Inside an organization
• **Secretary General**: the head of the organization
• **spokesperson**: the official voice of the organization to the media
• **officials**: people with official positions
• **resolution**: a formal decision or statement
• **constitution**: the fundamental set of principles or laws
• **court**: the place where disputes are resolved
• **headquarters**: the main office (e.g. the WHO headquarters is in Geneva)
"""

V = [
    mc(E, "Which organization is concerned with health issues and fighting diseases?", ["WHO", "FAO", "TI", "UNESCO"], img="🏥"),
    mc(E, "Which organization fights hunger and ensures food security?", ["FAO", "WHO", "AI", "UNHCR"], img="🌾"),
    mc(E, "Which organization protects refugees?", ["UNHCR", "TI", "FAO", "WHO"], img="⛺"),
    mc(E, "Which organization helps children and mothers?", ["UNICEF", "TI", "FAO", "AI"], img="👶"),
    mc(E, "Which organization fights corruption?", ["Transparency International", "UNICEF", "WHO", "FAO"]),
    mc(E, "Which organization defends human rights and political prisoners?", ["Amnesty International", "FAO", "WHO", "UNESCO"]),
    mc(E, "Which organization is concerned with education, science and culture?", ["UNESCO", "FAO", "ICRC", "TI"], img="📚"),
    mc(E, "Which organization works to keep world peace?", ["UN", "FAO", "TI", "WHO"], img="🕊️"),
    mc(E, "WHO stands for…", ["World Health Organization", "World Help Organization", "Work and Health Office", "World Human Organization"]),
    mc(E, "The head of an organization is the…", ["Secretary General", "spokesperson", "court", "resolution"]),
    tf(E, "The ICRC protects victims of armed conflicts and natural disasters.", True, img="⛑️"),
    tf(E, "FAO is concerned with fighting corruption.", False, "FAO fights hunger; TI fights corruption."),

    mc(M, "The ___ of the WHO is located in Geneva, Switzerland.", ["headquarters", "spokesperson", "resolution", "constitution"]),
    mc(M, "The ___ is responsible for managing the organization's day-to-day operations.", ["Secretary General", "court", "constitution", "resolution"]),
    mc(M, "A ___ is often passed by international assemblies to address global issues.", ["resolution", "headquarters", "spokesperson", "court"]),
    mc(M, "Each international organization operates according to its ___, which outlines its purpose and rules.", ["constitution", "spokesperson", "headquarters", "officials"]),
    mc(M, "When conflicts arise between member states, the ___ plays a key role in resolving them.", ["court", "spokesperson", "resolution", "headquarters"]),
    mc(M, "The ___ delivers official statements and answers questions from the media.", ["spokesperson", "Secretary General", "court", "constitution"]),
    mc(M, "UNICEF stands for United Nations International Children's Emergency ___.", ["Fund", "Force", "Forum", "Food"]),
    mc(M, "FAO stands for Food and ___ Organization.", ["Agriculture", "Aid", "Animals", "Assistance"]),
    mc(M, "Providing shelter and securing resettlement in another country is the job of…", ["UNHCR", "TI", "WHO", "UNESCO"]),
    wr(M, "UN stands for the United ___.", ["nations"]),
    wr(M, "AI stands for ___ International.", ["amnesty"]),

    wr(H, "What does UNESCO stand for? United Nations Educational, Scientific and ___ Organization.", ["cultural"]),
    wr(H, "What does ICRC stand for? International Committee of the ___ Cross.", ["red"]),
    wr(H, "What does UNHCR stand for? United Nations High Commissioner for ___.", ["refugees"]),
    wr(H, "A formal decision made by an organization is a ___.", ["resolution"]),
    wr(H, "A person who represents the views of an organization to the media is a ___.", ["spokesperson", "spokesman", "spokeswoman"]),
    wr(H, "The main office of an organization is its ___.", ["headquarters", "hq"]),
    mc(H, "Which pair is WRONG?", ["TI → fighting hunger", "WHO → fighting diseases", "UNHCR → caring for refugees", "AI → protecting human rights"]),
    mc(H, "A judicial body that resolves disputes with laws is a…", ["court", "constitution", "resolution", "spokesperson"]),
    mc(H, "Freeing political prisoners is a concern of…", ["Amnesty International", "FAO", "UNESCO", "WHO"]),
    tf(H, "A “constitution” is a set of fundamental principles or laws.", True),
]

GRAMMAR = """
# Linking words: review (from units 3, 4 and 5)
This unit has no new grammar lesson, so it reviews linking words. The booklet practises them in “Function: Linking Words (Review & Practice)”.

# Purpose
• **to / in order to / so as to + verb** · **so as not to / in order not to + verb**
• **so that + subject + can/could/will/would + verb** · **for + noun**

# Cause
• **because / since / as + clause** · **because of / due to / owing to / thanks to + noun**

# Effect
• **so** + clause · **therefore / thus / as a result / consequently,** + new sentence

# Concession
• **although / though / even though + clause** · **despite / in spite of + noun / -ing**
• **however / nevertheless,** + new sentence · **but / yet / whereas**

# Addition
• **and / also / as well as** · **moreover / furthermore / in addition,** · **in addition to / besides / along with + noun / -ing** · **not only … but also**
"""

G = [
    mc(E, "Many people learn computer skills ___ get better jobs.", ["so as to", "because", "although"], "purpose"),
    mc(E, "Taoufik couldn't concentrate ___ family problems.", ["because of", "because", "so that"], "cause + noun"),
    mc(E, "Souad didn't come to the show ___ she was sick.", ["since", "therefore", "despite"], "cause + clause"),
    mc(E, "We couldn't get financial support. ___, we had to abandon the project.", ["Therefore", "Although", "Because"], "effect"),
    mc(E, "Jamila attended the meeting ___ being sick.", ["despite", "although", "because"], "concession + -ing"),
    mc(E, "Smoking is very dangerous, ___ it should be forbidden in public places.", ["so", "because", "despite"]),
    mc(E, "Which linking word expresses PURPOSE?", ["in order to", "however", "because of", "moreover"]),
    mc(E, "Which linking word expresses CONCESSION?", ["although", "therefore", "so that", "in addition"]),
    mc(E, "Which linking word expresses EFFECT?", ["as a result", "since", "due to", "despite"]),
    mc(E, "Which linking word expresses CAUSE?", ["due to", "however", "so as to", "furthermore"]),
    tf(E, "“In spite of” and “despite” have the same meaning.", True),

    mc(M, "Hamid did a great job, ___ he wasn't offered a better position.", ["but", "so", "because"]),
    mc(M, "The government launched a project ___ reduce illiteracy.", ["in order to", "so that", "because of"]),
    mc(M, "Aziza did a nice job, ___ the teacher rewarded her.", ["so", "although", "despite"]),
    mc(M, "Many countries have natural resources. ___, they still suffer from poverty.", ["However", "Therefore", "Moreover"]),
    mc(M, "Jane was often absent. ___, her director dismissed her.", ["As a result", "However", "Although"]),
    mc(M, "The police evacuated the place ___ a suspicious bag in the garden.", ["due to", "because", "so that"]),
    mc(M, "___ plastic bags are toxic, many people still use them.", ["Although", "Despite", "Because of"]),
    mc(M, "I need to save money ___ I can start my own business.", ["so that", "so as to", "in order to"]),
    mc(M, "We rarely play music at night so as ___ disturb our neighbours.", ["not to", "to not", "don't"]),
    wr(M, "Halima got a promotion ___ she worked hard. (cause, starts with “s”)", ["since"]),
    wr(M, "A lot of youngsters go abroad because ___ unemployment.", ["of"]),

    mc(H, "Join: “Hamid did a great job. He wasn't offered a better position.” (though)", ["Though Hamid did a great job, he wasn't offered a better position.", "Though of his great job, Hamid wasn't offered a position.", "Hamid did a great job though he was offered a better position."]),
    mc(H, "Join: “Plastic bags are toxic. Many people still use them.” (in spite of)", ["In spite of the toxicity of plastic bags, many people still use them.", "In spite of plastic bags are toxic, many people still use them.", "In spite of many people use them, plastic bags are toxic."]),
    mc(H, "Join: “Some workers emigrate. They don't like low salaries.” (therefore)", ["Some workers don't like low salaries. Therefore, they emigrate.", "Some workers emigrate. Therefore, they don't like low salaries.", "Therefore some workers emigrate low salaries."]),
    mc(H, "Rewrite with “in spite of”: “Although Tom is afraid of dogs, he got a new dog.”", ["In spite of being afraid of dogs, Tom got a new dog.", "In spite of Tom is afraid of dogs, he got a new dog.", "In spite of Tom got a new dog, he is afraid of dogs."]),
    mc(H, "Rewrite with “due to”: “Hamza got the job because he is highly qualified.”", ["Hamza got the job due to his high qualifications.", "Hamza got the job due to he is highly qualified.", "Due to Hamza got the job, he is qualified."]),
    wr(H, "Rewrite with “though”: Many people buy iPhones in spite of their high price. → Many people buy iPhones though they ___ very expensive.", ["are"]),
    wr(H, "Rewrite with “to”: Whenever I need to get rid of stress, I take some days off. → I take some days off ___ get rid of stress.", ["to"]),
    wr(H, "Complete with ONE linking word: David was very sick. ___, he went to work.", ["however", "nevertheless", "yet", "still"]),
    wr(H, "Complete with ONE linking word: The light went off, ___ I didn't finish my homework.", ["so"]),
    mc(H, "Which sentence is WRONG?", ["Despite he was sick, David went to work.", "Although he was sick, David went to work.", "Despite being sick, David went to work.", "David was sick. However, he went to work."]),
    mc(H, "Which sentence is WRONG?", ["Camellia will rent a car in order to traveling.", "Camellia will rent a car to travel.", "Camellia will rent a car so as to travel.", "Camellia will rent a car so that she can travel."]),
]

FUNCTIONS = """
# Sharing news
• You know what! … · I've got something to tell you! · Guess what! · I've got some good / bad news.

# Responding to good news
• Wow, that sounds exciting! · Sounds awesome! · How fantastic! · That's great!
• My goodness! Great news! · Congratulations! · I'm so happy for you!

# Responding to bad news
• Sorry to hear that! · I really feel bad. · That's awful! · That's terrible!
• That's unfortunate. · That's really disappointing. · Oh no! · What a pity!

# Tip
Use **Congratulations!** for personal success (exam, marriage, new job). Use **Sorry to hear that** for illness, accidents and problems.
"""

F = [
    mc(E, "Which expression responds to GOOD news?", ["How fantastic!", "That's awful!", "Sorry to hear that!", "I really feel bad."], img="🎉"),
    mc(E, "Which expression responds to BAD news?", ["Sorry to hear that!", "Congratulations!", "Sounds awesome!", "That's great!"], img="😢"),
    mc(E, "“I passed my BAC exam!” — “___”", ["Congratulations!", "That's awful!", "I really feel bad.", "Never mind."]),
    mc(E, "“My father had an accident yesterday.” — “___”", ["Oh no! Sorry to hear that.", "How fantastic!", "Congratulations!", "Sounds awesome!"]),
    mc(E, "“I'm getting married next month.” — “___”", ["Congratulations! I'm so happy for you.", "That's really disappointing.", "Sorry to hear that.", "Enough is enough."]),
    mc(E, "“The UN just approved a new peacekeeping mission.” — “___”", ["That's fantastic! This could help the area.", "That's really disappointing.", "I really feel bad.", "Sorry to hear that."]),
    tf(E, "“That's unfortunate” is a response to bad news.", True),
    tf(E, "“Congratulations!” is a good response to “I lost my job.”", False),
    mc(E, "Which expression introduces news?", ["Guess what!", "Never mind!", "With pleasure!", "I beg your pardon?"]),
    mc(E, "“Wow, that sounds ___!”", ["exciting", "excited", "excite"]),

    mc(M, "“The negotiations broke down, and the UN couldn't find an agreement.” — Best response:", ["That's really disappointing.", "Wow, that sounds exciting!", "Congratulations!", "With pleasure."]),
    mc(M, "“Mom, I was accepted in the Canadian visa lottery!” — Best response:", ["My goodness! Great news!", "Sorry to hear that.", "That's awful.", "I'm not satisfied."]),
    mc(M, "“Sorry for my long absence. My sister was sick and I took care of her.” — Best response:", ["Sorry to hear that. I hope she's better now.", "How fantastic!", "Congratulations!", "Sounds awesome!"]),
    mc(M, "“You know what! A new sports club will open in our neighbourhood.” — Best response:", ["How fantastic!", "I really feel bad. That's awful.", "With pleasure."]),
    mc(M, "“People should use eco-friendly transport to save the environment.” — Best response:", ["I couldn't agree more.", "I am sorry, I have no time.", "Enough is enough."], "This is an opinion, so we agree or disagree."),
    mc(M, "“Would it be possible to show me how to use this machine?” — Best response:", ["Yes, why not.", "I share the same view.", "My apologies."], "This is a request."),
    wr(M, "Complete the response to bad news: Sorry to ___ that!", ["hear"]),
    wr(M, "Complete the response to good news: That ___ awesome! (one word)", ["sounds"]),
    wr(M, "Complete the response to bad news: That's really ___. (d___________)", ["disappointing"]),
    wr(M, "Complete the expression to share news: I've got something to ___ you!", ["tell"]),

    mc(H, "A: “___” B: “Wow, that sounds exciting!”", ["I've won a trip to Japan!", "My phone was stolen.", "I failed my driving test.", "My cat is sick."]),
    mc(H, "A: “___” B: “My goodness! That's awful!”", ["There was a terrible earthquake last night.", "I got a scholarship!", "We won the match!", "I'm having a party."]),
    mc(H, "Which response is NOT appropriate to “I've just got my driving licence!”?", ["That's unfortunate.", "Congratulations!", "That's great!", "How fantastic!"]),
    mc(H, "Which response is NOT appropriate to “Our team lost the final.”?", ["Sounds awesome!", "What a pity!", "That's really disappointing.", "Oh no!"]),
    mc(H, "“I really feel bad.” is a response to…", ["bad news", "good news", "a request", "a complaint"]),
    wr(H, "Give the noun of “congratulate”.", ["congratulations", "congratulation"]),
    wr(H, "Give the adjective of “disappoint” used for a situation (That's really ___).", ["disappointing"]),
    wr(H, "Give the opposite of “fortunate”.", ["unfortunate"]),
    mc(H, "Which function does “Guess what! I got the job!” express?", ["sharing good news", "responding to bad news", "complaining", "asking for advice"]),
    tf(H, "“What a pity!” is used to respond to bad news.", True),
]

UNIT = unit(8, "International Organizations",
            ("International organizations", VOCAB, V),
            ("Linking words (review)", GRAMMAR, G),
            ("Responding to news", FUNCTIONS, F))
