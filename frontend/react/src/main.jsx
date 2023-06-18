import React from 'react'
import ReactDOM from 'react-dom/client'
import {ChakraProvider} from '@chakra-ui/react'
import './index.css'
import { createBrowserRouter, RouterProvider} from "react-router-dom";

import { createStandaloneToast } from '@chakra-ui/react'
import Login from "./components/login/Login.jsx";
import AuthProvider from "./components/context/AuthContext.jsx";
import ProtectedRoute from "./components/shared/ProtectedRoute.jsx";
import Signup from "./components/signup/Signup.jsx";
import Customer from "./Customer.jsx";
import Home from "./Home.jsx";
const { ToastContainer } = createStandaloneToast()
const router = createBrowserRouter([
    {
        path: "/signup",
        element: <Signup />
    },
    {
        path: "/",
        element: <Login />
    },
    {
        path: "dashboard",
        element: <ProtectedRoute><Home /></ProtectedRoute>
    },
    {
        path: "dashboard/customers",
        element: <ProtectedRoute><Customer /></ProtectedRoute>
    }
]);

ReactDOM.createRoot(document.getElementById('root')).render(
  <React.StrictMode>
      <ChakraProvider>
          <AuthProvider>
              <RouterProvider router={router} />
          </AuthProvider>
          <ToastContainer />
      </ChakraProvider>
  </React.StrictMode>,
)
