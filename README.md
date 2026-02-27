# RN Barber Shop - Sistema de Agendamento

Sistema completo de agendamento para barbearia com:

- Página do cliente (agendamento online + redirecionamento automático para WhatsApp)
- Painel administrativo (agenda do dia, serviços, bloqueios, relatórios)
- API REST em Java com Spring Boot
- Banco de dados PostgreSQL
- Controle de versão de banco com Flyway

---

## 🚀 Tecnologias utilizadas

- Java 17
- Spring Boot
- Spring Security
- PostgreSQL
- Flyway
- HTML / CSS / JavaScript

---

## 📦 Funcionalidades

- Cadastro de serviços
- Agendamento com verificação de horário disponível
- Bloqueio de dias
- Relatório semanal e mensal
- Login administrativo protegido
- Integração automática com WhatsApp

---

## ▶️ Como rodar localmente

1. Configure um banco PostgreSQL local
2. Ajuste as variáveis no `application.properties`
3. Execute:

```bash
./mvnw spring-boot:run