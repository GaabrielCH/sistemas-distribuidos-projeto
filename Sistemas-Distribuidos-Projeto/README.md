### PROJETO DE SISTEMAS DISTRIBUÍDOS- 2024/1 UTFPR

#### Descrição do Projeto

O projeto consiste em um sistema de gerenciamento de candidatos, onde é possível realizar operações de 
CRUD em um banco de dados MySQL. O sistema é composto por um servidor e um cliente, 
que se comunicam através de sockets. O servidor é responsável por receber as requisições do cliente, processá-las e 
enviar uma resposta. O cliente é responsável por enviar as requisições ao servidor.


#### Funcionalidades

O sistema possui as seguintes funcionalidades:
    - Cadastrar um novo candidato(token jwt)
    - Listar o candidato especifico(token jwt)
    - Atualizar os dados de um candidato(token jwt)
    - Deletar o candidato especifico(token jwt)

    - Login(token jwt)
    - Logout(token jwt)

. O candidato so poderá requisitar a operação de listar, atualizar e deletar se estiver logado no sistema.


#### Estrutura do Projeto

. O sistema possui um sistema de autenticação baseado em tokens JWT (JSON Web Token) que sao usados para fazer as validações das operações.
. O sistema troca dados em forma de JSON entre o cliente e o servidor.

. as dependencias do projeto estão listadas no arquivo `pom.xml`, e são gerenciadas pelo Maven. Nas dependencias está comentado a bilioteca especifica.

. bibliotecas utilizadas no projeto :
    - auth0:java-jwt : https://mvnrepository.com/artifact/com.auth0/java-jwt/4.4.0
    - googlecode.json-simple : para manipulação de JSON - https://mvnrepository.com/artifact/com.googlecode.json-simple/json-simple/1.1.1
    - hibernate orm core : para mapeamento objeto-relacional -  https://mvnrepository.com/artifact/org.hibernate.orm/hibernate-core/6.5.0.Final
    - hibernate core : https://mvnrepository.com/artifact/org.hibernate/hibernate-core/5.3.22.Final
    - hibernate entity manager : para gerenciamento de entidades - https://mvnrepository.com/artifact/org.hibernate/hibernate-entitymanager/5.6.8.Final 
    - json web token : para gerar e validar tokens JWT - https://mvnrepository.com/artifact/io.jsonwebtoken/jjwt/0.12.5
    - mysql-connector-java : para conexão com o banco de dados MySQL - https://mvnrepository.com/artifact/mysql/mysql-connector-java/8.0.30
    - mysql-connector-J : https://mvnrepository.com/artifact/com.mysql/mysql-connector-j/8.3.0


#### Execução do Projeto

. Para executar o projeto, é necessário iniciar o MySQL.
. O arquivo `client.sql` contém o script para criar o banco de dados e a tabela necessária para o projeto, 
no client.sql já contem email e senhas que já foram cadastradas.
. O projeto está localizada em um unico pacote de Client/services.


.Gabriel Camlofski Horst


