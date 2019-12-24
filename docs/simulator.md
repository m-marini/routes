# Simulator

[TOC]

## Vehicle movement

Lo spostamento di un veicolo in una tratta è semplificato vincolando la velocità del veicolo al massimo consentito nel tratto, il limite di tempo simulato, la distanza di sicurezza con l'eventuale veicolo sucessivo e il limite di spazio della tratta. Si assume un'accelerazione istantanea illimitata.

La simulazione deve calcolare la nuova posizione del veicolo e il tempo impegato per raggiungere la nuova posizione.

La distanza di sicurezza tra un'auto e la sucessiva è determinata da

```math
    s = v \cdot t_r + l_v
```

dove

- $ v $ la velocità del veicolo
- $ t_r = 1 s $ tempo di reazione e
- $ l_v = 5m $ la lunghezza del veicolo

Il numero di veicoli presenti nel tratto è dato allora da

```math
    n = \frac{l_s}{s} = \frac{l_s}{v \cdot t_r+ l_v}
```

dove

- $ l_s $ è la lunghezza della tratta

Il numero massimo di veicoli in una tratta è quando i veicoli sono completamete fermi

```math
    n_x = \frac{l_s}{l_v}
```

Il numero massimo di veicoli con il massimo flusso invece è dato da

```math
    n_n = \frac{l_s}{v_x \cdot t_r+ l_v}
```

con $ v_x $ velocità massima nella tratta.

Possiamo calcolare il grado di intasamento relativo di una tratta come rapporto tra l'eccesso di veicoli rispetto il flusso massimo e l'eccesso massimo della tratta:

```math
    \nu = \frac{n - n_n}{n_x -n_n}
```

Il veicolo davanti a tutti ha come vincoli solo la velocità massima, il limite di tempo e il limite di spazio della tratta quindi la posizione successiva sarà

```math
    s_i' = \min (s_i + v_x \Delta t, l_s)
```

e il tempo reale di spostamento sarà

```math
    \Delta t' = \frac{s_i' - s_i}{v_x}
```

Tutti i veicoli precedenti si muoveranno mantenendo la distanza di sicurezza e il limite di tempo ovvero

```math
    v = \frac{\Delta s}{\Delta t}\\
    \Delta s + s = s_{i+1}-s_i \\
    \Delta s + v t_r + l_v = s_{i+1}-s_i \\
    \Delta s + \frac{\Delta s}{\Delta t} t_r+l_v = s_{i+1}-s_i \\
    \Delta s = \frac{s_{i+1}-s_i - l_v}{1+\frac{t_r}{\Delta t}} \\
    \Delta s = (s_{i+1}-s_i - l_v) \frac{\Delta t}{\Delta t + t_r}
```

## Simulation process

La fase di simulazione consiste di 

### Spostamento dei veicoli nelle tratte

- Spostamento di tutti i veicoli in una tratta partendo da quello davanti a tutti.
- Si calcola lo spazio percorso nell'intervallo di tempo determinato.
- Se il veicolo non esce dalla tratta si procede con il calcolo della posizione dei rimanenti veicoli in ordine di posizione.
- Se il veicolo esce dalla tratta si deve calcolare il tempo necessario ad arrivare alla fine della tratta
- posizionare il veicolo alla fine della tratta
- calcolare la posizione dei rimanenti veicoli in base all'intervallo di tempo intercorso per arrivare alla fine della tratta.

### Filtro tratte non ancora complete

Una volta processate tutte le tratte

- si filtrano le tratte con tempo di simulazione inferiore all'intervallo di simulazione.

### Selezione degli incroci

Abbiamo ora un'insieme di tratte il cui tempo di simulazione è diverso e dobbiamo decidere quali veicoli possono spostarsi di tratta.

La regola è che tra i veicoli che si intersecano sullo stesso nodo nell'intervallo del tempo di reazione hanno la precedenza quelli sulle tratte a priorità e in caso di stessa priorità la precedenza è ai veicoli provenienti da destra.

- Si raggruppano le tratte per nodo di arrivo
- Si ordinano per tempo di simulazione,
- Partendo dal primo

### Selezione precedenze

- Si esaminano le tratte con tempo di simulazione successivo entro il tempo di reazione

- Tra queste si selezionano quelle a priorità maggiore
- Tra queste si prende quella più a destra,
- Se non c'è ne nessuna più a destra (incrocio a pari priorità) si seleziona quella con tempo di simulazione minore (il primo arrivato all'incrocio).

### Spostamento veicolo a fine tratta

- Si calcola la tratta successiva per il veicolo della tratta selezionata o si elimina se ritornato alla partenza.
- il veicolo della viene spostato alla tratta sucessiva se è libera
- Si calcolando la posizione per il tempo dato dalla differenza tra il tempo di simulazione della tratta successiva e la tratta in uscita
- Tutti le tratte con tempo minore vengono fermate per il tempo della tratta selezionata.
- Se la tratta è occupata tutte le tratte con tempo minore o uguale vengono fermate per il tempo della tratta in uscita.
- Si ripete di nuovo il processo fino a quando tutte le tratte hanno completato il tempo di simulazione.

La fase di spostamento genera una nuova configurazione di veicoli nel tempo e una nuova mappa di tempi di transito.