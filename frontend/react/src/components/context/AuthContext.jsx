import { createContext, useContext, useEffect, useState} from "react"
import {login as preformLogin} from "../../services/client.js"
import jwtDecode from "jwt-decode";
const AuthContext = createContext({})
const AuthProvider = ({children}) => {
    const [customer, setCustomer] = useState(null);
    const setCustomerAndToken = () => {
        let token = localStorage.getItem("access_token");
        if(token){
            const customerJson = localStorage.getItem("customer");
            const customerObj = JSON.parse(customerJson);
            setCustomer({...customerObj});
        }
    }
    useEffect(() => {
        setCustomerAndToken();
    }, [])

    const login = async (credentials) => {
        return new Promise((resolve, reject) => {
            preformLogin(credentials).then(res => {
                const jwtToken = res.headers["authorization"];
                localStorage.setItem("access_token", jwtToken);
                localStorage.setItem("customer", JSON.stringify({...res.data.customerDTO}));
                setCustomerAndToken();
                resolve(res)
            }).catch(err => {
                reject(err)
            })
        })
    }
    const logOut = () => {
        localStorage.removeItem("access_token");
        localStorage.removeItem("customer");
        setCustomer(null);
    }
    const isCustomerAuthenticated = () => {
        const token = localStorage.getItem("access_token");
        if(!token){
            return false;
        }
        const { exp: expiration } = jwtDecode(token);
        if(Date.now() > expiration * 1000){
            logOut();
            return false;
        }
        return true;
    }
    return(
        <AuthContext.Provider value={{
            customer, login, logOut, isCustomerAuthenticated, setCustomerAndToken
        }}>
            {children}
        </AuthContext.Provider>
    );
}
export const useAuth = () => useContext(AuthContext);
export default AuthProvider;