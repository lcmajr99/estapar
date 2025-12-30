# 📘 Functional Specification – Parking Event-Driven Backend

Este documento descreve o **comportamento funcional**, as **regras de negócio** e as **decisões arquiteturais** adotadas no sistema **Parking Event-Driven Backend**.

O objetivo é tornar explícito **como o sistema funciona**, **por que certas decisões foram tomadas** e **como os eventos impactam o estado do estacionamento**.

---

## 1. Inicialização do Sistema (StartupConfig)

No momento da inicialização da aplicação, o sistema realiza a **leitura e carga dos dados iniciais** por meio da classe `StartupConfig`.

### Responsabilidades do StartupConfig

- Carregar os **setores do estacionamento**
- Definir:
  - Código do setor
  - Capacidade máxima
  - Preço base por hora
  - Horário de funcionamento
  - Limites operacionais
- Criar as **vagas físicas** associadas a cada setor
- Garantir que o sistema inicie em um **estado consistente**

### Motivo da Decisão

A leitura inicial via `StartupConfig` garante que:
- O sistema seja **autossuficiente**
- Não dependa de chamadas externas para inicialização
- Facilite testes locais e ambientes controlados
- Evite estados inválidos em produção

---

## 2. Modelo Orientado a Eventos

O sistema é orientado a eventos e processa o ciclo de vida de um veículo a partir de três eventos principais:

- `ENTRY`
- `PARKED`
- `EXIT`

Cada evento representa uma **mudança de estado específica** e possui responsabilidades bem definidas.

---

## 3. Evento ENTRY – Entrada Lógica do Veículo

### Descrição

O evento `ENTRY` representa a **intenção de entrada** de um veículo no estacionamento.  
Nesse momento, o sistema **ainda não sabe onde o veículo irá estacionar fisicamente**.

### Responsabilidades

- Verificar se o veículo já possui uma sessão ativa (idempotência)
- Selecionar um **setor disponível**
- Aplicar o **fator de preço dinâmico**
- Criar uma nova sessão de estacionamento
- Incrementar a ocupação lógica do setor

### O que NÃO acontece no ENTRY

- Não há alocação de vaga física
- Não há validação de coordenadas GPS
- Não há bloqueio de uma vaga específica

### Decisão Arquitetural

**A vaga física NÃO é alocada no ENTRY.**

Motivo:
- No mundo real, o motorista pode:
  - Circular
  - Mudar de setor
  - Estacionar em local diferente do previsto
- Alocar vaga física cedo demais criaria:
  - Estados inválidos
  - Conflitos artificiais
  - Complexidade desnecessária

---

## 4. Evento PARKED – Estacionamento Físico

### Descrição

O evento `PARKED` representa o **momento real em que o veículo estaciona fisicamente**, identificado por coordenadas GPS (latitude e longitude).

### Responsabilidades

- Localizar a vaga física com base nas coordenadas
- Validar se a vaga existe
- Verificar conflitos físicos (vaga já ocupada)
- Conciliar setor lógico x setor físico
- Atualizar a sessão com a vaga física real

### Reconciliação de Setor

Se o veículo estacionar em um setor diferente daquele previsto no `ENTRY`:

- A ocupação do setor original é decrementada
- A ocupação do novo setor é incrementada
- O setor da sessão é atualizado
- O fator de preço é recalculado
- Um log de reconciliação é registrado

### Decisão Arquitetural

**A alocação física acontece somente no PARKED.**

Motivo:
- Reflete o comportamento real do usuário
- Permite mudanças de decisão do motorista
- Evita inconsistências entre mundo lógico e físico
- Simplifica o modelo de concorrência

---

## 5. Evento EXIT – Saída do Veículo

### Descrição

O evento `EXIT` representa a **finalização da sessão de estacionamento**.

### Responsabilidades

- Validar se existe uma sessão ativa
- Validar integridade temporal (saída após entrada)
- Calcular o tempo total de permanência
- Aplicar a regra de tolerância gratuita (30 minutos)
- Calcular o valor final
- Liberar vaga física
- Atualizar ocupação do setor
- Finalizar a sessão

### Regra de Tolerância

- Permanência ≤ 30 minutos → **R$ 0,00**
- Permanência > 30 minutos → cobrança por hora cheia

---

## 6. Preço Dinâmico por Setor

### Descrição

O sistema aplica **preço dinâmico baseado na ocupação de cada setor**, e não na ocupação total da garagem.

### Fatores de Preço

O fator é calculado com base na razão:

ocupação atual do setor / capacidade máxima do setor


Exemplo de faixas:
- < 25% → desconto
- 25% a 50% → preço base
- 50% a 75% → acréscimo
- \> 75% → sobretaxa

### Decisão Arquitetural

**O preço dinâmico é definido por setor, não globalmente.**

Motivos:
- Setores podem ter:
  - Localização diferente
  - Valor estratégico diferente
  - Demanda distinta
- Evita penalizar usuários de setores vazios
- Modelo mais realista e escalável
- Facilita expansão futura (ex: setores premium)

---

## 7. Consistência e Transações

Cada evento é processado em seu próprio **handler transacional**, garantindo:

- Atomicidade
- Consistência
- Isolamento
- Rollback em caso de falha

Não há chamadas internas entre métodos transacionais (*self-invocation*).

---

## 8. Logs e Auditoria

O sistema registra logs funcionais para:

- Criação de sessão
- Confirmação de estacionamento
- Mudança de setor
- Finalização de sessão

Logs não expõem dados controlados pelo usuário diretamente.

---

## 9. Considerações Finais

O sistema foi projetado para:

- Refletir comportamentos reais
- Evitar estados artificiais
- Manter clareza de regras de negócio
- Ser facilmente testável
- Suportar evolução futura

Cada decisão foi tomada priorizando **consistência**, **simplicidade** e **aderência ao mundo real**.

---

# 🚗 Parking Event-Driven Backend

Backend desenvolvido em **Java + Spring Boot**, orientado a eventos, para simular e gerenciar o fluxo de veículos em um sistema de estacionamento urbano.

---

## 🧱 Stack Tecnológica

- **Java 21**
- **Spring Boot 3**
- **Spring Web**
- **Spring Data JPA**
- **Hibernate**
- **PostgreSQL**
- **JUnit 5**
- **Mockito**
- **JaCoCo**
- **SonarCloud**
- **Maven**
- **Docker**
- **GitHub Actions**

---

## 📖 Descrição do Projeto

O **Parking Event-Driven Backend** é uma aplicação backend projetada para lidar com um cenário real de estacionamento urbano utilizando uma **arquitetura orientada a eventos**.

O sistema recebe eventos de um simulador externo (via webhook) que representam o ciclo de vida de um veículo dentro do estacionamento. Esses eventos são processados para manter a consistência lógica e física, aplicar regras de negócio, calcular preços e gerar relatórios de faturamento.

O projeto foi desenvolvido com foco em:

- Arquitetura limpa
- Separação clara de responsabilidades
- Consistência transacional
- Testabilidade
- Qualidade de código
- Integração com CI/CD

---

## 🧭 Fluxo de Eventos

O sistema processa três tipos principais de eventos:

### ENTRY
- Cria uma nova sessão de estacionamento
- Valida idempotência
- Seleciona um setor disponível
- Aplica preço dinâmico com base na ocupação
- Não aloca vaga física

### PARKED
- Resolve a vaga física real usando coordenadas GPS
- Concilia setor lógico e setor físico
- Recalcula o preço em caso de mudança de setor
- Trata conflitos físicos
- Confirma o estado de estacionamento

### EXIT
- Calcula o tempo total de permanência
- Aplica período de tolerância gratuito (30 minutos)
- Calcula o valor final
- Libera a vaga física e a ocupação do setor
- Finaliza a sessão de estacionamento

---

## 💰 Módulo de Faturamento (Revenue)

O projeto expõe uma funcionalidade de cálculo de faturamento que permite:

- Consulta de receita por data
- Filtro opcional por setor
- Resposta padronizada contendo:
  - valor total
  - moeda
  - timestamp

Essa lógica é isolada em um serviço dedicado, seguindo o **Princípio da Responsabilidade Única (SRP)**.

---

## 🧩 Visão Geral da Arquitetura

A aplicação evita serviços monolíticos ao dividir responsabilidades em componentes dedicados:

- `ParkingService` – Roteador de eventos
- `EntryEventHandler` – Lógica de entrada
- `ParkedEventHandler` – Lógica de estacionamento físico
- `ExitEventHandler` – Lógica de saída e faturamento
- `RevenueService` – Cálculo de receita
- Camada de repositórios por agregado

Essa abordagem garante:
- Fronteiras transacionais corretas
- Ausência de *self-invocation* com proxies do Spring
- Testes unitários mais simples
- Evolução segura do código

---

## 🧪 Testes & Qualidade

- Testes unitários por handler
- Testes de roteamento de eventos
- Cobertura de código gerada com **JaCoCo**
- Análise estática e Quality Gate via **SonarCloud**
- Pipeline de CI automatizado com **GitHub Actions**

---

## 🚀 CI/CD

A cada **push** ou **pull request**, o pipeline executa:

1. Build do projeto
2. Execução dos testes
3. Geração de cobertura
4. Análise no SonarCloud

O pipeline pode ser configurado para bloquear merges caso o Quality Gate falhe.

---

## 🎯 Objetivo do Projeto

Este projeto foi desenvolvido para demonstrar:

- Boas práticas de backend
- Arquitetura orientada a eventos
- Uso correto de transações no Spring
- Código limpo e manutenível
- Fluxo profissional de CI/CD e qualidade de código

---

## ▶️ Como Executar o Projeto

### Pré-requisitos

- Docker  
- Docker Compose v2  

Verifique com:

```bash
docker --version
docker compose version
````

---

### 1. Subir o ambiente com Docker Compose

Na raiz do projeto, execute:

```bash
docker compose up -d --build
```

Esse comando irá iniciar:

* Banco de dados MySQL
* Garage Simulator
* Aplicação Spring Boot

---

### 2. Validar o Banco de Dados

Verifique se os containers estão rodando:

```bash
docker compose ps
```

O banco deve aparecer como:

```text
estapar-mysql   running (healthy)
```

Acessar o banco (opcional):

```bash
docker exec -it estapar-mysql mysql -u estapar -p
```

Senha:

```text
estapar
```

---

### 3. Validar a Configuração Inicial (StartupConfig)

A classe `StartupConfig` é executada no startup da aplicação e é responsável por inicializar:

* Setores
* Capacidades
* Preços base
* Vagas físicas

Para validar, acompanhe os logs:

```bash
docker compose logs -f app
```

Procure por mensagens indicando inicialização bem-sucedida.

---

### 4. Validar a Aplicação

A aplicação ficará disponível em:

```text
http://localhost:3003
```

O simulador externo estará disponível em:

```text
http://localhost:3000
```

---

### Testar Eventos Manualmente (Opcional)

Exemplo de evento `ENTRY`:

```http
POST http://localhost:3003/events
Content-Type: application/json
```

```json
{
  "license_plate": "ABC-1234",
  "event_type": "ENTRY",
  "entry_time": "2025-01-01T10:00:00",
  "lat": -23.5,
  "lng": -46.6
}
```

---

### Parar o Ambiente

Parar os containers:

```bash
docker compose down
```

Parar e remover volumes (resetar banco):

```bash
docker compose down -v
```

---


## 👨‍💻 Developer

**Luis Carlos**  
Backend Developer  

