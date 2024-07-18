package Client.services;

import java.io.Serializable;

public class Recruiter implements Serializable {

        private static final long serialVersionUID = 1L;
        private int id;

        private String name;
        private String email;

        private String password;

        private String industry;
        private String description;

        public Recruiter(int id, String name, String email, String password, String industry, String description) {
            this.id = id;
            this.name = name;
            this.email = email;
            this.password = password;
            this.industry = industry;
            this.description = description;
        }

        public Recruiter() {
            this.id = 0;
            this.name = "";
            this.email = "";
            this.password = "";
            this.industry = "";
            this.description = "";
            // Inicialize todos os outros campos com valores padrão
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public String getIndustry() {
            return industry;
        }

        public void setIndustry(String industry) {
            this.industry = industry;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }



}
