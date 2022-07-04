# Air traffic management system
Un progetto di **Michele Cirillo** per il corso di Internet Based Distributed Simulation (IBDS) dell'università "Tor Vergata" in Roma.

## Analisi dei requisiti
- Ogni aeroporto è provvisto di 2 piste per l'atterraggio/decollo
	- In caso entrambe siano occupate, si adotta una politica FIFO per la coda d'attesa
- Un aereo che arriva ad un aeroporto attende un tempo predefinito al gate e successivamente decolla verso 
un altro aeroporto (che potrebbe essere parte del sistema o no).
- Gli aerei sono caratterizzati da proprietà come *aeroporto*, *aeroporto di destinazione*, 
*tempo di viaggio*
- Gli arrivi e le partenze ad ogni aeroporto sono schedulate secondo un calendario 
giornaliero predefinito.