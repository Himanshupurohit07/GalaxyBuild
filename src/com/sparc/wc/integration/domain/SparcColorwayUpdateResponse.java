package com.sparc.wc.integration.domain;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SparcColorwayUpdateResponse {

    public enum ErrorType {
        ERROR, WARNING;
    }


    public static class Error {
        private String id;
        private ErrorType errorType = ErrorType.ERROR;
        private String error;


        //<editor-fold defaultstate="collapsed" desc="delombok">
        @SuppressWarnings("all")
        public static class ErrorBuilder {
            @SuppressWarnings("all")
            private String id;
            @SuppressWarnings("all")
            private ErrorType errorType;
            @SuppressWarnings("all")
            private String error;

            @SuppressWarnings("all")
            ErrorBuilder() {
            }

            @SuppressWarnings("all")
            public SparcColorwayUpdateResponse.Error.ErrorBuilder id(final String id) {
                this.id = id;
                return this;
            }

            @SuppressWarnings("all")
            public SparcColorwayUpdateResponse.Error.ErrorBuilder errorType(final ErrorType errorType) {
                this.errorType = errorType;
                return this;
            }

            @SuppressWarnings("all")
            public SparcColorwayUpdateResponse.Error.ErrorBuilder error(final String error) {
                this.error = error;
                return this;
            }

            @SuppressWarnings("all")
            public SparcColorwayUpdateResponse.Error build() {
                return new SparcColorwayUpdateResponse.Error(this.id, this.errorType, this.error);
            }

            @Override
            @SuppressWarnings("all")
            public String toString() {
                return "SparcColorwayUpdateResponse.Error.ErrorBuilder(id=" + this.id + ", errorType=" + this.errorType + ", error=" + this.error + ")";
            }
        }

        @SuppressWarnings("all")
        public static SparcColorwayUpdateResponse.Error.ErrorBuilder builder() {
            return new SparcColorwayUpdateResponse.Error.ErrorBuilder();
        }

        @SuppressWarnings("all")
        public String getId() {
            return this.id;
        }

        @SuppressWarnings("all")
        public ErrorType getErrorType() {
            return this.errorType;
        }

        @SuppressWarnings("all")
        public String getError() {
            return this.error;
        }

        @SuppressWarnings("all")
        public void setId(final String id) {
            this.id = id;
        }

        @SuppressWarnings("all")
        public void setErrorType(final ErrorType errorType) {
            this.errorType = errorType;
        }

        @SuppressWarnings("all")
        public void setError(final String error) {
            this.error = error;
        }

        @SuppressWarnings("all")
        public Error() {
        }

        @SuppressWarnings("all")
        public Error(final String id, final ErrorType errorType, final String error) {
            this.id = id;
            this.errorType = errorType;
            this.error = error;
        }
        //</editor-fold>
    }

    private Set<String> updated = new HashSet<>();
    private List<Error> errors = new ArrayList<>();


    //<editor-fold defaultstate="collapsed" desc="delombok">
    @SuppressWarnings("all")
    public static class SparcColorwayUpdateResponseBuilder {
        @SuppressWarnings("all")
        private Set<String> updated;
        @SuppressWarnings("all")
        private List<Error> errors;

        @SuppressWarnings("all")
        SparcColorwayUpdateResponseBuilder() {
        }

        @SuppressWarnings("all")
        public SparcColorwayUpdateResponse.SparcColorwayUpdateResponseBuilder updated(final Set<String> updated) {
            this.updated = updated;
            return this;
        }

        @SuppressWarnings("all")
        public SparcColorwayUpdateResponse.SparcColorwayUpdateResponseBuilder errors(final List<Error> errors) {
            this.errors = errors;
            return this;
        }

        @SuppressWarnings("all")
        public SparcColorwayUpdateResponse build() {
            return new SparcColorwayUpdateResponse(this.updated, this.errors);
        }

        @Override
        @SuppressWarnings("all")
        public String toString() {
            return "SparcColorwayUpdateResponse.SparcColorwayUpdateResponseBuilder(updated=" + this.updated + ", errors=" + this.errors + ")";
        }
    }

    @SuppressWarnings("all")
    public static SparcColorwayUpdateResponse.SparcColorwayUpdateResponseBuilder builder() {
        return new SparcColorwayUpdateResponse.SparcColorwayUpdateResponseBuilder();
    }

    @SuppressWarnings("all")
    public Set<String> getUpdated() {
        return this.updated;
    }

    @SuppressWarnings("all")
    public List<Error> getErrors() {
        return this.errors;
    }

    @SuppressWarnings("all")
    public void setUpdated(final Set<String> updated) {
        this.updated = updated;
    }

    @SuppressWarnings("all")
    public void setErrors(final List<Error> errors) {
        this.errors = errors;
    }

    @SuppressWarnings("all")
    public SparcColorwayUpdateResponse() {
    }

    @SuppressWarnings("all")
    public SparcColorwayUpdateResponse(final Set<String> updated, final List<Error> errors) {
        this.updated = updated;
        this.errors = errors;
    }
    //</editor-fold>
}
