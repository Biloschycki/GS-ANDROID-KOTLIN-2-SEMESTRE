# Hora Certa

> Seu remédio na hora certa: sem esquecer e sem tomar dobrado.

Aplicativo Android (Kotlin + Jetpack Compose) desenvolvido para a **Global Solution 2026 – 2º semestre (FIAP)**, desafio **CitizenTech**, disciplina *Android Kotlin Developer*.

**Repositório:** [github.com/Biloschycki/GS-2SEMESTRE-ANDROD-KOTLIN](https://github.com/Biloschycki/GS-2SEMESTRE-ANDROD-KOTLIN)

## Integrantes

| Nome completo | RM |
|---|---|
| Enzo Biloschycki Magalhães Cesar | RM556736 |
| Vitor Portela Fantinato | RM554540 |
| Nicolas Varella Barros Padovam | RM556586 |

---

## Problema

Idosos com doenças crônicas (hipertensão, diabetes, colesterol) costumam tomar vários remédios por dia, cada um em um horário. Esquecer uma dose, **tomar a mesma dose duas vezes** ou confundir as caixas é comum. O familiar que cuida geralmente trabalha fora e acaba ligando várias vezes ao dia só para perguntar "já tomou?". Erros de adesão ao tratamento pioram o controle da doença e aumentam idas ao pronto-socorro.

## Público beneficiado

- **Pessoas idosas** que usam remédios de uso contínuo, muitas com baixa visão e pouca familiaridade com celular.
- **Familiares e cuidadores** que acompanham o tratamento.

## Proposta

Um app simples, de letras grandes e botões amplos, que:

1. Aceita dois tipos de horário: **horário fixo** (uma vez por dia, ex.: 08:00) ou **recorrência** (ex.: "a cada 8 horas" a partir das 08:00 → 08:00, 16:00 e 00:00).
2. **Avisa na hora certa** com notificação **e leitura em voz alta** do nome do remédio, da dose e da observação ("caixa branca e azul, tomar em jejum").
3. Permite confirmar com um único botão grande: **TOMEI**.
4. **Bloqueia a dose dobrada**: se a dose da vez já foi tomada, o app avisa para não tomar de novo.
5. **Repete o aviso** 15 minutos depois se a dose não foi confirmada.
6. Guarda o **histórico** de doses e avisa quando o **estoque está acabando**.

Tudo funciona **offline**, com os dados salvos apenas no aparelho (Room). É um protótipo acadêmico: o app não se integra a farmácias, médicos ou serviços públicos.

## ODS relacionados

### ODS 3 – Saúde e bem-estar
Seguir corretamente o tratamento de doenças crônicas reduz complicações e internações evitáveis. O app atua diretamente na **adesão ao tratamento**, contribuindo para a **meta 3.4** (reduzir a mortalidade prematura por doenças não transmissíveis, como as cardiovasculares e o diabetes). O bloqueio de dose dobrada também reduz riscos de intoxicação por medicamento.

### ODS 10 – Redução das desigualdades
Muitas soluções digitais de saúde excluem quem tem baixa visão, pouca escolaridade ou pouca familiaridade com tecnologia. O Hora Certa foi pensado para esse público: fonte grande, alto contraste, uma ação principal por cartão, **voz como canal principal** e status que não depende só de cor (ícone + texto). Isso se conecta à **meta 10.2** (promover a inclusão social de todos, independentemente da idade ou deficiência).

---

## Funcionalidades

| Requisito | Onde está no app |
|---|---|
| Tela inicial com identidade visual | **Início**: nome, objetivo, público, resumo do dia ("2 de 3 doses tomadas"), atalhos grandes |
| Navegação com Navigation Compose | 5 telas: Início, Meus remédios, Cadastro/Edição, Detalhe e Sobre |
| Cadastro e visualização | Cadastro de remédio (nome, dosagem, **horário fixo ou recorrência em horas**, estoque, observação) e listagem |
| Ações extras sobre o registro | **Marcar como tomado**, **editar**, **excluir** (com confirmação) e **ligar/desligar lembrete** |
| Validação | Nome (mín. 2 letras), dosagem e horário obrigatórios; recorrência de 1 a 24 horas; estoque numérico de 0 a 999 |
| Feedback e estados vazios | Snackbars ("Dose de Losartana registrada"), mensagens de lista vazia e de histórico vazio |
| Persistência com Room | Remédios e histórico de doses ficam salvos após fechar e abrir o app |
| ViewModel + estado | Um ViewModel por tela, expondo `StateFlow` observado pelo Compose |
| Extra (opcional) | Lembrete por alarme exato, notificação com botões **TOMEI** / **Lembrar em 10 min**, voz (TextToSpeech), repetição automática e reagendamento após reiniciar o celular |

### Entidades (Room)

```kotlin
@Entity(tableName = "medicamentos")
data class Medicamento(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val nome: String,
    val dosagem: String,
    val hora: Int,
    val minuto: Int,
    val tipoHorario: TipoHorario = TipoHorario.FIXO, // FIXO ou RECORRENTE
    val intervaloHoras: Int = 24,                    // recorrência: a cada N horas
    val referencia: Long? = null,                    // recorrência: instante da 1ª dose
    val observacao: String = "",
    val estoque: Int = 0,
    val lembreteAtivo: Boolean = true,
    val ultimaTomada: Long? = null,
    val dataCriacao: Long = System.currentTimeMillis()
)

@Entity(tableName = "registros_dose") // histórico, com ForeignKey CASCADE
data class RegistroDose(val id: Long = 0, val medicamentoId: Long, val tomadoEm: Long)
```

**Como o app sabe se a dose foi tomada:**

- **Horário fixo:** a dose conta como tomada quando `ultimaTomada` cai na data de hoje. À meia-noite tudo "zera" sozinho, sem tarefa em segundo plano.
- **Recorrência:** as doses formam a sequência `referencia`, `referencia + N h`, `referencia + 2N h`... A dose da vez conta como tomada se `ultimaTomada` estiver dentro da janela atual. A janela começa até 1 hora antes do horário, para quem toma um pouco adiantado. Quando chega o próximo horário, uma nova dose é liberada.
- Se a recorrência é cadastrada depois do horário inicial (ex.: início 08:00, a cada 8 h, cadastrado às 20:00), o primeiro lembrete segue a sequência e toca à 00:00.

As regras ficam em funções puras (`util/Recorrencia.kt`, `domain/RegrasDose.kt`) e são cobertas por testes unitários.

---

## Tecnologias

- **Kotlin** e **Jetpack Compose** com **Material 3**
- **Navigation Compose** (rotas com argumentos `LongType`)
- **ViewModel** + **StateFlow** / `stateIn` + `collectAsStateWithLifecycle`
- **Room** (Entity, DAO com `Flow`, `withTransaction`, ForeignKey)
- **Hilt** (injeção de dependência: `@HiltViewModel`, módulos `@Provides`/`@Binds`, `@EntryPoint` nos receivers)
- **Coroutines**
- **AlarmManager** (alarme exato), **BroadcastReceiver**, **NotificationCompat** e **TextToSpeech**
- Testes: **JUnit 4** (regras de horário e validação) e testes instrumentados do repositório com **Room em memória**

## Arquitetura

```
br.com.fiap.horacerta
├── HoraCertaApplication.kt   @HiltAndroidApp, cria o canal de notificação
├── MainActivity.kt               @AndroidEntryPoint, pede permissão e abre o NavHost
├── data/
│   ├── local/                    Medicamento, RegistroDose, MedicamentoDao, AppDatabase
│   └── repository/               MedicamentoRepository (interface) + Impl
├── domain/                       RegrasDose (fixo × recorrência) e RegistrarDoseUseCase
├── di/                           DatabaseModule, RepositoryModule (Hilt)
├── lembrete/                     AgendadorLembretes, LembreteReceiver, BootReceiver,
│                                 NotificacaoHelper, LeitorDeVoz, LembreteEntryPoint
├── ui/
│   ├── navigation/               Screen (rotas) e AppNavHost
│   ├── inicio/                   InicioScreen + InicioViewModel
│   ├── medicamentos/             MedicamentosScreen + MedicamentosViewModel
│   ├── formulario/               FormularioScreen + FormularioViewModel + ValidacaoFormulario
│   ├── detalhe/                  DetalheScreen + DetalheViewModel (histórico)
│   ├── sobre/                    SobreScreen (problema, público, ODS)
│   ├── comum/                    componentes reutilizáveis e leitura em voz alta
│   └── theme/                    cores, tipografia ampliada e tema
└── util/                         HorarioUtil e Recorrencia (funções puras de data/hora)
```

Fluxo de dados (MVVM): **Tela (Compose)** → chama função do **ViewModel** → **Repository** → **Room**. O Room devolve `Flow`, o ViewModel transforma em `StateFlow` e a tela se redesenha sozinha quando os dados mudam.

Cada tela é dividida em `XxxScreen` (conectada ao ViewModel) e `XxxContent` (sem estado, só recebe dados e callbacks), o que permite `@Preview` sem banco de dados.

---

## Fluxo principal

1. Na **Início**, o usuário toca em **Novo** e cadastra "Losartana – 50 mg – Horário fixo – 08:00". Um antibiótico pode ser cadastrado como "Amoxicilina – 500 mg – Recorrência: 8 horas – primeira dose 08:00".
2. O app salva no Room, agenda o próximo alarme e volta mostrando "Losartana cadastrado. Lembrete: todo dia às 08:00."
3. No horário, o celular mostra a notificação e **fala**: *"Hora do remédio. Losartana, 50 mg. Caixa branca e azul."*
4. O usuário toca em **TOMEI** (na notificação ou no app). A dose vai para o histórico, o estoque cai de 1 e o cartão fica verde: "Tomado às 08:02".
5. Se tentar marcar de novo, o app avisa: "Losartana já foi tomado hoje. Não tome de novo." Na recorrência, o aviso é: "Esta dose de Amoxicilina já foi tomada. A próxima é às 16:00."
6. Em **Meus remédios** é possível editar, excluir ou desligar o lembrete; no **Detalhe** aparece o histórico.

## Evidências de funcionamento

Todas as capturas foram feitas no emulador (Medium Phone, API 37) e estão em [`docs/prints/`](docs/prints/). Os vídeos estão em [`docs/videos/`](docs/videos/).

### Vídeos (com som)

| Vídeo | O que mostra |
|---|---|
| [lembrete-na-hora-com-voz.mp4](docs/videos/lembrete-na-hora-com-voz.mp4) | O app aguardando o horário (17:00). Ao chegar a hora, a notificação aparece com os botões **TOMEI** e **Lembrar em 10 min**, e o celular lê o lembrete em voz alta. |
| [status-hora-de-tomar.mp4](docs/videos/status-hora-de-tomar.mp4) | Com dois remédios cadastrados, a Novalgina das 17:35 passa de "Ainda não é a hora" para **"Hora de tomar!"** exatamente na virada do minuto, junto com a notificação (por volta de 1:32 do vídeo). |
| [botao-ouvir.mp4](docs/videos/botao-ouvir.mp4) | Botão **Ouvir** no cartão do remédio: o app lê o nome, a dose e a observação em voz alta. |

| Notificação do lembrete | Status "Hora de tomar!" |
|---|---|
| ![Notificação do lembrete na hora certa](docs/prints/miniaturas/27-notificacao-lembrete.png) | ![Status Hora de tomar](docs/prints/miniaturas/29-hora-de-tomar.png) |

### 1. Primeira abertura

| Permissão de notificações | Tela inicial vazia | Sobre o projeto e ODS |
|---|---|---|
| ![permissao notificacoes](docs/prints/miniaturas/01-permissao-notificacoes.png) | ![inicio vazio](docs/prints/miniaturas/02-inicio-vazio.png) | ![sobre](docs/prints/miniaturas/22-sobre.png) |

### 2. Cadastro com horário fixo

| Formulário | Escolhendo a hora | Escolhendo o minuto | Preenchido |
|---|---|---|---|
| ![cadastro vazio](docs/prints/miniaturas/03-cadastro-vazio.png) | ![selecionando hora](docs/prints/miniaturas/04-selecionando-hora.png) | ![selecionando minuto](docs/prints/miniaturas/05-selecionando-minuto.png) | ![cadastro horario fixo](docs/prints/miniaturas/06-cadastro-horario-fixo.png) |

O lembrete com som e voz pode ser desligado já no cadastro ([print](docs/prints/07-cadastro-lembrete-desligado.png)).

**Validação:** ao tocar em **Salvar** sem preencher, cada campo obrigatório mostra o que falta, em vermelho e logo abaixo do campo.

![Validação do formulário](docs/prints/miniaturas/28-validacao.png)

### 3. Cadastro com recorrência

| Opção "Recorrência" | Recorrência preenchida com prévia dos lembretes |
|---|---|
| ![cadastro recorrencia](docs/prints/miniaturas/08-cadastro-recorrencia.png) | ![cadastro recorrencia previa](docs/prints/miniaturas/09-cadastro-recorrencia-previa.png) |

### 4. Tela inicial e ação extra: TOMEI

| Doses de hoje | Dose atrasada | Dose tomada (fixo) | Dose tomada (recorrência) |
|---|---|---|---|
| ![inicio com remedios](docs/prints/miniaturas/10-inicio-com-remedios.png) | ![dose atrasada](docs/prints/miniaturas/26-dose-atrasada.png) | ![dose tomada fixo](docs/prints/miniaturas/11-dose-tomada-fixo.png) | ![dose tomada recorrencia](docs/prints/miniaturas/12-dose-tomada-recorrencia.png) |

### 5. Meus remédios: lembrete, edição e exclusão

| Lista | Lembrete desligado | Lembrete ligado |
|---|---|---|
| ![meus remedios](docs/prints/miniaturas/13-meus-remedios.png) | ![lembrete desligado](docs/prints/miniaturas/14-lembrete-desligado.png) | ![lembrete ligado](docs/prints/miniaturas/15-lembrete-ligado.png) |

| Editando (50 mg → 60 mg) | Editado | Confirmação de exclusão | Excluído |
|---|---|---|---|
| ![editando](docs/prints/miniaturas/16-editando.png) | ![editado](docs/prints/miniaturas/17-editado.png) | ![excluindo](docs/prints/miniaturas/19-excluindo.png) | ![excluido](docs/prints/miniaturas/20-excluido.png) |

### 6. Detalhe e histórico de doses

![Detalhe do remédio com histórico](docs/prints/miniaturas/18-detalhe-historico.png)

### 7. Persistência com Room

| 1. Registro salvo | 2. App fechado (sem apps recentes) | 3. Abrindo o app de novo | 4. Dados mantidos |
|---|---|---|---|
| ![inicio apos exclusao](docs/prints/miniaturas/21-inicio-apos-exclusao.png) | ![persistencia app fechado](docs/prints/miniaturas/23-persistencia-app-fechado.png) | ![persistencia reabrindo](docs/prints/miniaturas/24-persistencia-reabrindo.png) | ![persistencia dados mantidos](docs/prints/miniaturas/25-persistencia-dados-mantidos.png) |

O remédio "Roacutam", cadastrado com recorrência, continua salvo depois de o app ser encerrado pela tela de apps recentes e aberto novamente.

---

## Como executar

1. Clone o repositório:
   ```
   git clone https://github.com/Biloschycki/GS-2SEMESTRE-ANDROD-KOTLIN.git
   ```
2. Abra o **Android Studio** (versão compatível com AGP 9.2) e escolha **Open** → pasta clonada (`GS-2SEMESTRE-ANDROD-KOTLIN`).
3. Aguarde o **Gradle Sync** baixar as dependências.
4. Rode no emulador ou em um aparelho com **Android 7.0 (API 24) ou superior** (botão ▶ *Run 'app'*).
5. Na primeira abertura, **permita as notificações**.

### Testando o lembrete rapidamente

1. Cadastre um remédio com horário **2 minutos à frente** do relógio do aparelho.
2. Volte para a tela inicial do Android (pode bloquear a tela).
3. No horário, a notificação aparece e o texto é lido em voz alta.
4. Toque em **TOMEI** na notificação e abra o app: o cartão estará verde.

> **Voz no emulador:** se não ouvir nada, verifique em *Configurações → Acessibilidade → Saída de texto para fala* se há voz em português instalada e se o volume de mídia do emulador está ligado.
>
> **Aparelhos Xiaomi, Samsung, Motorola etc.:** alguns fabricantes bloqueiam alarmes para economizar bateria. Se o lembrete não tocar, libere o app em *Bateria → Sem restrições*.

### Testes

- Unitários (JVM): `./gradlew test` (regras de horário fixo, recorrência, próxima dose e validação do formulário).
- Instrumentados (emulador ligado): `./gradlew connectedAndroidTest` (Room + repositório: salvar, editar, registrar dose, bloquear dose dobrada no fixo e na recorrência, excluir em cascata).

---

## Limitações e próximos passos

- O alerta ao familiar quando a dose não é confirmada **não está implementado** (exigiria backend). Hoje o app repete o aviso para o próprio usuário.
- Próximos passos: perfil do cuidador com alerta remoto, leitura da receita por foto (OCR) e relatório de adesão para levar à consulta.
