from build_units import *

VOCAB = """
# Sources of humour
• **jokes**: short stories that make people laugh
• **comedy films / comedy plays / sitcoms** (situation comedies on TV)
• **cartoons**: films using animation instead of real people
• **caricature**: a funny drawing that exaggerates someone's features
• **pranks**: practical jokes or tricks played on someone
• **memes / trolls**: funny images or videos shared online
• **one-man show / comedy show**

# People
• **humorist**, **comedian**, **jokes teller** (a person good at telling jokes), **caricaturist**

# Verbs
• **to laugh**, **to crack / tell a joke**
• **to kid with someone**: to have fun with someone
• **to make fun of someone**: to laugh at someone in a negative way
• **to play a prank / trick on someone**

# Adjectives
• **funny**, **amusing**
• **witty**: funny in an intelligent way
• **silly**: funny in a stupid way
• **hilarious**: extremely funny

# Collocations
• **sense of humour**: the ability to understand and enjoy jokes
• **burst into laughter**: to suddenly start laughing very hard
• **facial expressions**: movements of the face
• **black humour**: looking at serious things in an amusing way
"""

V = [
    mc(E, "What do we call a film that uses animation instead of real people?", ["a cartoon", "a sitcom", "a prank", "a caricature"], img="🐭"),
    mc(E, "What do we call a funny drawing that exaggerates a person's features?", ["a caricature", "a meme", "a sitcom", "a joke"], img="✏️"),
    mc(E, "A practical joke or trick played on someone is a…", ["prank", "comedy play", "humorist", "sense"], img="🎭"),
    mc(E, "A person who is good at telling jokes is a…", ["jokes teller", "caricature", "cartoon", "prank"], img="🎤"),
    mc(E, "Which word means “extremely funny”?", ["hilarious", "silly", "serious", "sad"], img="🤣"),
    mc(E, "Which word means “funny in an intelligent way”?", ["witty", "silly", "rude", "boring"]),
    mc(E, "Which word means “funny in a stupid way”?", ["silly", "witty", "clever", "hilarious"]),
    mc(E, "A TV comedy series about the same characters in everyday situations is a…", ["sitcom", "documentary", "news report", "caricature"], img="📺"),
    mc(E, "Funny images shared on social media are…", ["memes", "plays", "speeches", "lectures"], img="📱"),
    tf(E, "“To make fun of someone” means to laugh at them in a negative way.", True),
    tf(E, "“To kid with someone” means to fight with them.", False, "It means to have fun with someone."),

    mc(M, "He's very funny. Whenever I meet him, he ___ a joke.", ["tells", "says", "speaks", "makes fun"]),
    mc(M, "He really has a good ___ of humour.", ["sense", "feeling", "taste", "idea"], "Collocation: sense of humour"),
    mc(M, "Charlie Chaplin was one of the most creative ___.", ["humorists", "humours", "jokes", "laughters"]),
    mc(M, "The show was hilarious. It caused a lot of ___ in the audience.", ["laughter", "laugh", "laughing at", "laughed"]),
    mc(M, "When the clown fell, all the children burst into ___.", ["laughter", "humour", "jokes", "funny"], "Collocation: burst into laughter"),
    mc(M, "Black humour is a way of looking at ___ things in an amusing way.", ["serious", "funny", "silly", "witty"]),
    mc(M, "People from Marrakesh excel at ___ jokes.", ["telling", "saying", "making fun", "laughing"]),
    mc(M, "It's not nice to ___ people who are different from you.", ["make fun of", "kid with", "laugh with", "joke for"]),
    mc(M, "Comedians use their facial ___ to make people laugh.", ["expressions", "laughters", "humours", "pranks"]),
    wr(M, "The president's ___ speech attracted a lot of admiration. (funny in an intelligent way)", ["witty"]),
    wr(M, "He is very ___. He makes me laugh a lot. (adjective from “fun”)", ["funny"]),

    wr(H, "Give the adjective: something that makes you laugh a lot is ___ (h______).", ["hilarious"]),
    wr(H, "Complete the collocation: burst into ___.", ["laughter", "laughing"]),
    wr(H, "Complete the collocation: facial ___.", ["expressions", "expression"]),
    wr(H, "A person who draws caricatures is a ___.", ["caricaturist", "cartoonist"]),
    wr(H, "To play a trick on someone is to play a ___ on them.", ["prank", "joke", "practical joke"]),
    mc(H, "Which word is the odd one out?", ["serious", "witty", "hilarious", "amusing"], "The others all mean funny."),
    mc(H, "Which sentence uses the word correctly?", ["She has a great sense of humour.", "She has a great sense of laughter.", "She burst into humour.", "She told a funny prank."]),
    mc(H, "“His jokes are clever and make you think.” His jokes are…", ["witty", "silly", "rude", "boring"]),
    mc(H, "“To kid with someone” is the opposite of…", ["to be serious with someone", "to have fun with someone", "to joke with someone", "to laugh with someone"]),
    tf(H, "A “sitcom” is a funny drawing in a newspaper.", False, "A sitcom is a situation comedy on TV; a funny drawing is a caricature or cartoon."),
]

GRAMMAR = """
# Present modals (modal + bare infinitive)
• **can**: ability (Jack can swim well) / permission (Can I use your phone?) / request (Can you help me?)
• **can't**: inability (He can't climb, his leg hurts) / impossibility (She can't be your sister; you look different)
• **could**: polite request (Could you bring me tea?) / past ability (My granny could swim)
• **couldn't**: past inability (I couldn't understand English when I was young)
• **may**: polite permission (May I open the window?) / possibility (Alex may travel tomorrow)
• **might**: possibility (It might rain later)
• **must**: obligation (People must respect the law) / certainty (She is angry. She must have problems)
• **mustn't**: prohibition (You mustn't use phones in class)
• **should / shouldn't**: advice (You look tired. You should rest)
• **have to**: obligation, often external (I have to call the doctor)
• **don't have to / needn't**: no obligation (You needn't buy milk; I bought some)
• **will**: certainty about the future · **would**: unreal situation / polite request

# Past modals (modal + have + past participle)
• **could have + pp**: possible in the past, but it didn't happen (I could have done the homework, but I was busy)
• **would have + pp**: a past intention that didn't happen (I would have visited you, but I lost your address)
• **should have + pp**: criticism or regret about the past (You should have seen a doctor)
• **might / may have + pp**: past possibility, not sure (She might have taken the wrong bus)
• **must have + pp**: logical certainty about the past (He was angry. He must have had problems)
• **can't / couldn't have + pp**: impossibility in the past (She can't have said that; she wasn't there)
• **needn't have + pp**: you did it, but it wasn't necessary
"""

G = [
    mc(E, "Jack ___ swim well. He won the race.", ["can", "mustn't", "needn't"], "can = ability"),
    mc(E, "People ___ respect the law.", ["must", "might", "could"], "must = obligation"),
    mc(E, "You look tired. You ___ take some rest.", ["should", "can't", "mustn't"], "should = advice"),
    mc(E, "It's cloudy right now. It ___ rain later.", ["might", "must", "can't"], "might = possibility"),
    mc(E, "___ I open the window, please?", ["May", "Must", "Should"], "may = polite permission"),
    mc(E, "Muslims ___ eat or drink during the day in Ramadan.", ["mustn't", "needn't", "can"], "mustn't = prohibition"),
    mc(E, "I ___ sleep last night because I had a headache.", ["couldn't", "mustn't", "shouldn't"], "couldn't = past inability"),
    mc(E, "In “She might have taken the wrong bus”, the modal expresses…", ["possibility in the past", "obligation", "ability", "advice"]),
    mc(E, "After a modal we use…", ["the bare infinitive (e.g. go)", "to + infinitive (e.g. to go)", "a gerund (e.g. going)"]),
    tf(E, "“You needn't buy milk” means it is not necessary to buy milk.", True),
    tf(E, "“must have + past participle” expresses certainty about the past.", True),

    mc(M, "Kate doesn't answer the phone. She ___ be asleep.", ["must", "should", "needn't"], "must = logical certainty"),
    mc(M, "She ___ be your sister. You look completely different.", ["can't", "must", "should"], "can't = impossibility"),
    mc(M, "Amina ___ lie to you. She is an honest girl.", ["can't", "must", "has to"]),
    mc(M, "You ___ kill this snake. It's not dangerous.", ["needn't", "must", "have to"], "needn't = no necessity"),
    mc(M, "Ali ___ stay up late last night to finish his work.", ["had to", "must", "should"], "had to = past obligation"),
    mc(M, "Don't worry. My brother ___ fix your car. He is a mechanic.", ["can", "mustn't", "needn't"]),
    mc(M, "You ___ stopped smoking years ago. Now it's too late.", ["should have", "must have", "can have"], "should have + pp = criticism/regret"),
    mc(M, "Mr. Alex looked happy last night. He ___ bought the car he told you about.", ["must have", "should have", "needn't have"], "must have + pp = certainty about the past"),
    mc(M, "I couldn't find my laptop. It ___ been taken by John. I'm not sure.", ["might have", "must have", "should have"], "might have = not sure"),
    wr(M, "You ___ revise your lessons if you want to pass your exams. (obligation, 4 letters)", ["must"]),
    wr(M, "Drivers ___ slow down near schools. (advice)", ["should", "must"]),

    wr(H, "Mariam could have ___ (translate) this article, but she was busy.", ["translated"], "could have + past participle"),
    wr(H, "The students should have ___ (revise) before the exam. Now they got very low marks.", ["revised"]),
    wr(H, "Carlos ___ have answered all the questions, but the time was not enough. (ability)", ["could"]),
    wr(H, "Jack was very angry last night. He must have ___ (have) some problems.", ["had"]),
    wr(H, "Rewrite with a modal: “It's possible that she missed the bus.” → She ___ missed the bus.", ["might have", "may have", "could have"]),
    wr(H, "Rewrite with a modal: “I'm sure he forgot.” → He ___ forgotten.", ["must have"]),
    mc(H, "“I'm sure Lucy didn't steal it; she was at home.” → Lucy ___ it.", ["can't have stolen", "must have stolen", "should have stolen", "needn't have stolen"]),
    mc(H, "“It was a mistake not to tell her the truth.” → You ___ her the truth.", ["should have told", "must have told", "might have told", "could tell"]),
    mc(H, "Choose the correct sentence.", ["They would have started the tour, but the virus broke out.", "They would started the tour, but the virus broke out.", "They would have start the tour, but the virus broke out."]),
    mc(H, "“Kamal ___ give any explanations. Everything is clear.”", ["needn't", "must", "has to", "should have"]),
    tf(H, "“You needn't have bought bread” means you bought bread but it wasn't necessary.", True),
]

FUNCTIONS = """
# Expressing lack of understanding
• Sorry, I didn't get your point.
• I'm not sure I know what you mean.
• I don't see what you mean. / I don't see what you're getting at.
• I'm not sure I got your point.
• I beg your pardon?

# Asking for clarification
• What do you mean by …?
• Do you mean …?
• Can you explain what you mean by that?
• Could you clarify that, please?
• Could you repeat, please?

# Explaining / clarifying
• I mean … / It means that …
• In other words, …
• Let me explain that in more detail.
• Let me put it another way …

# Showing you understood
• Ah! Now I understand. / It's clear now, thank you. / I see.
"""

F = [
    mc(E, "Which expression ASKS for clarification?", ["What do you mean by that?", "In other words…", "I see, thanks.", "Let me explain."]),
    mc(E, "Which expression is used to EXPLAIN?", ["In other words, …", "Could you clarify that?", "I beg your pardon?", "What do you mean?"]),
    mc(E, "Which expression shows LACK OF understanding?", ["Sorry, I didn't get your point.", "Now I understand.", "It means that…", "Let me put it another way."]),
    mc(E, "“You know what? Sophie is two-faced.” — “Sorry, I didn't get your point.” The second speaker…", ["didn't understand", "agrees", "is explaining", "is refusing"], img="🤔"),
    mc(E, "Which expression shows you now understand?", ["Ah! Now I understand.", "I beg your pardon?", "What do you mean by that?", "I don't see what you mean."], img="💡"),
    tf(E, "“I beg your pardon?” can be used when you didn't hear or understand something.", True),
    tf(E, "“Let me put it another way” is used to ask for clarification.", False, "It is used to explain again."),
    mc(E, "Could you ___ that, please?", ["clarify", "clear", "clarification", "clearly"]),
    mc(E, "What do you ___ by “pranks”?", ["mean", "say", "tell", "explain"]),
    mc(E, "Let me explain that in more ___.", ["details", "points", "means", "words"]),

    mc(M, "“I beg your pardon. What do you mean by ‘silly’?” — Best answer:", ["It means funny in a stupid way.", "Could you clarify that, please?", "I share the same view."]),
    mc(M, "“I think we should use humour at school.” — You don't understand. You say:", ["Could you clarify that, please?", "That's exactly what I was thinking.", "It means a funny teacher."]),
    mc(M, "“I'm not quite following you. What do you mean by ‘pranks’?” — Best answer:", ["I mean a practical joke that we play on our friends.", "Yes, I completely agree.", "With pleasure."]),
    mc(M, "A: “Black comedy is…” B: “Sorry, I'm not sure I know what you mean.” A: “___”", ["It's a style of comedy that deals with serious issues.", "I beg your pardon?", "Do you mean…?", "Me neither."]),
    mc(M, "Which expression is NOT used to ask for clarification?", ["In other words…", "Do you mean…?", "Could you clarify that?", "What do you mean by…?"]),
    mc(M, "Which expression is NOT used to explain?", ["I don't see what you're getting at.", "I mean…", "Let me put it another way.", "It means that…"]),
    wr(M, "Complete: In other ___, he is not honest.", ["words"]),
    wr(M, "Complete: I don't see what you're getting ___.", ["at"]),
    wr(M, "Complete: Sorry, I didn't get your ___.", ["point"]),
    wr(M, "Complete: Let me ___ it another way. (verb)", ["put"]),

    mc(H, "Put the dialogue in order: (a) I mean you can't trust her. (b) Sophie is two-faced. (c) Could you explain what you mean? (d) Now I understand.", ["b – c – a – d", "c – b – a – d", "b – a – c – d", "a – b – c – d"]),
    mc(H, "Your friend says: “This film was a real tearjerker.” You don't know the word. The best reaction is:", ["Sorry, what do you mean by “tearjerker”?", "I completely agree with you.", "Could you lend me the film?", "You should have watched it."]),
    mc(H, "Which reply is NOT appropriate after “Could you clarify that, please?”", ["I'm afraid I can't lend it to you.", "I mean that…", "In other words, …", "Let me explain that in more detail."]),
    mc(H, "A: “Sophie gossips.” B: “___” A: “I mean she tells your secrets to others.”", ["What do you mean by that?", "Now I understand.", "With pleasure.", "I agree."]),
    wr(H, "Complete the question to ask for clarification: Can you explain what you ___ by that?", ["mean"]),
    wr(H, "Complete: Could you ___, please? I couldn't hear you. (ask someone to say it again)", ["repeat", "repeat that", "say that again"]),
    wr(H, "Complete the expression of lack of understanding: I'm not ___ I got your point.", ["sure"]),
    mc(H, "Which function does “Do you mean you won't come?” express?", ["asking for clarification", "making a request", "giving advice", "apologizing"]),
    mc(H, "Which function does “Let me explain that in more detail” express?", ["explaining / clarifying", "expressing regret", "refusing", "expressing lack of understanding"]),
    tf(H, "“I don't see what you're getting at” means “I don't understand what you are trying to say.”", True),
]

UNIT = unit(2, "Humour",
            ("Sources of humour", VOCAB, V),
            ("Present & past modals", GRAMMAR, G),
            ("Asking for clarification", FUNCTIONS, F))
