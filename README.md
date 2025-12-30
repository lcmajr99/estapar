# 🚗 Parking Event-Driven Backend

## 📌 Versão
**v1.0.0**

---

## 🧠 Nome do Projeto
**Parking Event-Driven Backend**

---

## 🧱 Stack Tecnológica

### Backend
- **Java 21**
- **Spring Boot 3**
- **Spring Web**
- **Spring Data JPA**
- **Hibernate**
- **Spring Transaction Management**

### Persistência
- **PostgreSQL**
- **JPA / Hibernate**

### Qualidade & Testes
- **JUnit 5**
- **Mockito**
- **JaCoCo (Code Coverage)**
- **SonarCloud (Quality Gate)**

### Build & DevOps
- **Maven**
- **Docker**
- **Docker Compose**
- **GitHub Actions (CI)**
- **SonarCloud Integration**

### Observabilidade
- **SLF4J**
- **Logback**

---

## 📖 Descrição do Projeto

O **Parking Event-Driven Backend** é uma aplicação backend desenvolvida em **Java com Spring Boot**, projetada para gerenciar um sistema de estacionamento urbano orientado a **eventos**, simulando o comportamento real de veículos em um ambiente controlado.

A aplicação recebe eventos externos (via webhook) que representam o ciclo de vida de um veículo dentro do estacionamento, desde sua entrada lógica até a saída física, incluindo reconciliação de setor, cálculo de preços dinâmicos e faturamento.

O projeto foi construído com foco em:

- Arquitetura limpa e modular
- Separação clara de responsabilidades
- Consistência transacional
- Qualidade de código
- Testabilidade
- Integração com ferramentas modernas de CI/CD

---

## 🧭 Modelo Orientado a Eventos

O sistema processa três tipos principais de eventos:

### 🚪 ENTRY
Evento que representa a **entrada lógica** do veículo no estacionamento.

Funcionalidades:
- Verifica idempotência (não permite sessões duplicadas)
- Seleciona setor com capacidade disponível
- Aplica **preço dinâmico** conforme ocupação
- Cria sessão de estacionamento sem vaga física definida

---

### 🅿️ PARKED
Evento que representa o **estacionamento físico real** do veículo.

Funcionalidades:
- Localiza vaga via coordenadas GPS (latitude/longitude)
- Valida divergência entre setor lógico e setor físico
- Recalcula preço em caso de mudança de setor
- Trata conflitos físicos de vaga
- Registra logs de confirmação ou reconciliação

---

### 🚗 EXIT
Evento que representa a **saída do veículo**.

Funcionalidades:
- Calcula tempo total de permanência
- Aplica regra de tolerância gratuita (30 minutos)
- Calcula valor final da estadia
- Libera vaga física e ocupação lógica
- Finaliza a sessão e registra logs financeiros

---

## 💰 Módulo de Faturamento (Revenue)

O sistema disponibiliza um módulo de faturamento que permite:

- Consulta de receita por data
- Filtro opcional por setor
- Retorno padronizado contendo:
  - valor total
  - moeda
  - timestamp da consulta

Esse módulo foi isolado em um serviço específico, respeitando o princípio de **Single Responsibility**.

---

## 🧩 Arquitetura

O projeto evita classes monolíticas e segue uma arquitetura modular:

- **ParkingService** → Roteador de eventos
- **EntryEventHandler** → Lógica de entrada
- **ParkedEventHandler** → Lógica de estacionamento físico
- **ExitEventHandler** → Lógica de saída e faturamento
- **RevenueService** → Cálculo de receita
- Repositórios separados por agregados

Essa abordagem garante:
- Transações reais via proxy Spring
- Eliminação de *self-invocation*
- Testes unitários mais simples e focados
- Evolução segura do código

---

## 🧪 Testes & Qualidade

O projeto conta com:

- Testes unitários por handler
- Testes de roteamento de eventos
- Cobertura de código com **JaCoCo**
- Análise contínua de qualidade via **SonarCloud**
- Pipeline CI com **GitHub Actions**

---

## 🚀 CI/CD

O pipeline automatizado executa:

1. Build do projeto
2. Execução dos testes
3. Geração de relatório de cobertura
4. Análise de qualidade no SonarCloud

Tudo é executado automaticamente em **push** e **pull request**.

---

## 🎯 Objetivo do Projeto

Demonstrar boas práticas de backend moderno, incluindo:

- Arquitetura orientada a eventos
- Uso correto de transações no Spring
- Separação de responsabilidades
- Código limpo e testável
- Integração com ferramentas de qualidade e CI/CD

---

## 👨‍💻 Desenvolvedor

**Luis Carlos**  
Backend Developer  
Java • Spring Boot • Arquitetura Orientada a Eventos
