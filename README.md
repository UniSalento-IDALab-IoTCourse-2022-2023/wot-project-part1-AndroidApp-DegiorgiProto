# Polar app android
Il sistema si prepone di risolvere il problema de monitoraggio di determinati parametri a domicilio con l'obiettivo di visualizzare lo stato di salute di una persona, nel particolare caso in questione un atleta o un paziente.
L'architettura è costituita da:
1. i dispositivi indossabili polar verity sense e polar H10
2. un'app android che permette l'acquisizione dei dati tramite BLE ANT+, la possibilità di aggiungere parametri misurati dall'utente in questione, come pressione arteriosa e diabete, la possibilità di aggiungere eventuali esercizi svolti dall'utente, la possibilitàdi visualizzare i propri dati in modo intellegibile attraverso dei grafici [repo app android](https://github.com/UniSalento-IDALab-IoTCourse-2022-2023/wot-project-part1-AndroidApp-DegiorgiProto)
3. un'app web utilizzabile dal medico che dà la possibilità di visualizzare i propri pazienti e i relativi dati acquisiti e aggiunti attraverso dei grafici [repo app web](https://github.com/UniSalento-IDALab-IoTCourse-2022-2023/wot-project-part3-WebApp-DegiorgiProto)
4. un backend costituito da un server che fa da intermediario con il database che raccoglie i dati acquisiti e che permette quindi di aggiungerli e prelevarli per la visualizzazione, e il database stesso. [repo backend](https://github.com/UniSalento-IDALab-IoTCourse-2022-2023/wot-project-part2-Backend-DegiorgiProto)

Questa repository in particolare riguarda l'app android, compresa di front end e backend.
La cartella _layout_ è lo scheletro del front end.
La cartella _androidblesdk_ contiene la business logic che permette la gestione del front end e due file che gestiscono il backend, ovvero la comunicazione con il server per la gestione dei dati e l'istanza di un web socket utilizzato per la gestione di eventuali notifiche push da parte del server stesso.

### ATTENZIONE
Tutti gli URL utilizzati sono personalizzabili; c'è infatti la possibilità di cambiare l'indirizzo IP con il proprio indirizzo corrispondente dalla wlan, con localhost o con un indirizzo IP corrispondente ad un servizio Cloud, a seconda di dove si trova il server e il database.
In questo caso specifico l'idea è stata di usare il servizio AWS EC2 su cui istanziare il database.
