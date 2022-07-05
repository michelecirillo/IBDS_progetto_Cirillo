---
title: Air traffic management system
author: Michele Cirillo
summary: prova priova prova
---

Un progetto di **Michele Cirillo** per il corso di Internet Based Distributed Simulation (IBDS) dell'università "Tor Vergata" in Roma.

# Requisiti applicativi
Si vuole studiare il caso di un sistema di gestione del traffico aereo, in particolare 
si vuole simulare l'arrivo, l'atterragio ed il decollo di aerei da e per diversi aereoporti (almeno 
2)\
Si voglio seguire i seguenti requisiti:

- Ogni aeroporto è provvisto di 2 piste per l'atterraggio/decollo
	- In caso entrambe siano occupate, si adotta una politica FIFO per la coda d'attesa
- Un aereo che arriva ad un aeroporto attende un tempo predefinito al gate e successivamente decolla verso 
un altro aeroporto (che potrebbe essere parte del sistema o no).
- Gli aerei sono caratterizzati da proprietà come *aeroporto*, *aeroporto di destinazione*, 
*tempo di viaggio*
- Gli arrivi e le partenze di ogni aeroporto sono schedulate secondo un calendario 
giornaliero predefinito.
	- Il sistema deve essere analizzato durante un'intera giornata operativa: dalle 5:00 
	AM alle 11:00 PM
	
## Ulteriori assunzioni
Consideriamo le seguenti assunzioni e/o limitazioni:

- A causa della limitazione del software implementativo, gli aereoporti saranno esattamente 2 
  (comunque almeno 2, come richiesto nei requisiti). Nello specifico, saranno: 
  - L'aeroporto di Roma Fiumicino (FCO)
  - L'aeroporto di Milano Linate (LIN) 
- Ogni aeroporto ha una torre di controllo che dà l'autorizzazione ad ogni aereo di 
atterrare o decollare

# Analisi dei requisiti
Consideriamo la seguente tabella:

|Req|Breve descrizione|Scelta progettuale|
|-|------|-----|
|R1|Ogni aeroporto ha 2 piste|Ariport Class, runway array attribute|
|R2|Un aereo che arriva attende e dopo decolla di nuovo|Una richiesta di atterraggio genera una nuova richiesta di decollo|
|R3|Gli aerei sono caratterizzati da aeroporto, aeroporto di destinazione e tempo di viaggio|Airplane Class; airport, destination airport and travel time attributes|
|R4|Calendario giornaliero per gli arrivi e le partenze|Calendar singleton, operationalDay Class|

# Preliminary Design 
Consideriamo il seguente UML Class Diagram:

![Federation Conceptual Model](./doc/federation_conceptual_model.jpg)

- *ATMSSImulation* contiene il main della simulazione, instanzia lo scenario ed il federato 
associato (FCO o LIN)
- *Airport* è il federato della simulazione
	- *code* indica il codice dell'aeroporto (FCO o LIN nel nostro esempio)
	- *runway* contiene due booleani che indicano se le rispettive piste sono occupate 
	o no
- *operationalDay* contiene tutti i voli schedulati in una giornata di lavoro dell'aeroporto corrispondente
- *Airplane* è una classe che identifica gli aerei, caratterizzati dall'*airport*, 
*destinationAirport* e *travelTime*
- *Event* indica un generico evento
	- *time* indica il timestamp dell'evento
	- *type* indica il tipo dell'evento
- _RemoteEvent_ indica un evento che un aeroporto genera per un altro
	- *airplane* è l'aereo coinvolto nell'evento, che in questo caso sarà un evento di 
	*landing request*
- _LocalEvent_ indica un evento locale all'aeroporto
	- *airplane* è l'aereo coinvolto nell'evento, che in questo caso sarà un evento di 
	*take off request* o *landing request*, in caso la pista fosse occupata al momento 
	dell'arrivo dell'aereo
- _AirplaneStatus_ è un'enum che contiene tutti i possibili stati in cui può trovarsi un'aereo 
della simulazione
- _EventType_ è un'enum che contiene tutti i possibili tipi di eventi (sia remote che 
local)

# Examples Scenario
## Esempio 1

Consideriamo la seguente illustrazione di un possibile scenario:

![scenario example 1](./doc/scenario_example1.jpg)

Assumiamo di star simulando le ore 6:00AM del 24 Ottobre e ci concentriamo sui singoli aerei. 
Assumiamo inoltre che il **travelTime** di ciascun aereo sia di 30 minuti.

- *AZ001* e *AZ002* stanno entrambi partendo dall'aeroporto FCO e stanno quindi occupando le due piste 
disponibili
- *AZ003* che è partito alle ore 5\:45 da LIN è **IN FLIGHT** verso FCO
- *AZ004* è nello stato **LANDED** in attesa di partire da LIN alle ore 3:00PM
- *AZ005* è partito alle ore 5\:30 da LIN ed è arrivato a FCO, sta aspettando che una delle due piste di FCO si 
liberi per poter atterrare. 
    