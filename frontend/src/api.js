import axios from "axios";


// ============================================================
// BANKCORE API CLIENT
// ============================================================

const API_BASE_URL =
    import.meta.env.VITE_API_URL ||
    "http://localhost:8080/api";


const api = axios.create({

    baseURL: API_BASE_URL,

    headers: {
        "Content-Type": "application/json",
    },

    timeout: 15000,

});


// ============================================================
// REQUEST INTERCEPTOR
// ============================================================

api.interceptors.request.use(

    (config) => {

        const token =
            localStorage.getItem("bankcore_token");

        if (token) {

            config.headers.Authorization =
                `Bearer ${token}`;
        }

        return config;
    },

    (error) => {

        return Promise.reject(error);

    }

);


// ============================================================
// RESPONSE INTERCEPTOR
// ============================================================

api.interceptors.response.use(

    (response) => {

        return response;

    },

    (error) => {

        if (error.response?.status === 401) {

            localStorage.removeItem(
                "bankcore_token"
            );

            localStorage.removeItem(
                "bankcore_user"
            );

        }

        return Promise.reject(error);

    }

);


export default api;