import {
    Button,
    Checkbox,
    Flex,
    FormControl,
    FormLabel,
    Heading,
    Input,
    Link,
    Stack,
    Image, Text, Alert, AlertIcon,
} from '@chakra-ui/react';
import {Formik, Form, useField} from "formik";
import * as Yup from "yup";
import {useAuth} from "../context/AuthContext.jsx";
import {errorNotification} from "../../services/notification.js";
import { useNavigate } from "react-router-dom";
import {useEffect} from "react";

const MyTextInput = ({ label, ...props }) => {
    // useField() returns [formik.getFieldProps(), formik.getFieldMeta()]
    // which we can spread on <input>. We can use field meta to show an error
    // message if the field is invalid and it has been touched (i.e. visited)
    const [field, meta] = useField(props);
    return (
        <>
            <FormLabel htmlFor={props.id || props.name}>{label}</FormLabel>
            <Input className="text-input" {...field} {...props} autoComplete={"off"} />
            {meta.touched && meta.error ? (
                <Alert className="error" status={"error"} mt={2}>
                    <AlertIcon />
                    {meta.error}
                </Alert>
            ) : null}
        </>
    );
};
const LoginForm = () => {
    const {login} = useAuth();
    const navigate = useNavigate();
    return(
        <Formik
            validateOnMount={true}
            validationSchema={
                Yup.object({
                    username: Yup.string()
                        .email("Must be valid email")
                        .required("Email is required"),
                    password: Yup.string()
                        .max(20, "Password can not be more than 20 characters")
                        .required("Password is required")
                })
            }
            initialValues={{
                username: '',
                password: ''
            }}
            onSubmit={(values, {setSubmitting}) => {
                setSubmitting(true);
                login(values).then(res => {
                    navigate("/dashboard/customers")
                }).catch(err => {
                    const error = err.response ? err.response.data.message : "Server not available!";
                    errorNotification(
                        err.code,
                        error
                    );
                }).finally(() => {
                    setSubmitting(false);
                })
            }}
        >
            {({isValid, isSubmitting, dirty}) => (
                <Form>
                    <Stack spacing={15} >
                        <MyTextInput label={"Email"} name={"username"} type={"email"} placeholder={"Enter your email"}/>
                        <MyTextInput label={"Password"} name={"password"} type={"password"} placeholder={"Enter your password"}/>
                        <Button type={"submit"} isDisabled={!(isValid && dirty) || isSubmitting}>Login</Button>
                    </Stack>
                </Form>
            )}
        </Formik>
    )
}

const Login = () => {
    const {isCustomerAuthenticated} = useAuth();
    const navigate = useNavigate();
    useEffect(() => {
        if(isCustomerAuthenticated()){
            navigate("/dashboard");
        }
    }, [])
    return (
        <Stack minH={'100vh'} direction={{ base: 'column', md: 'row' }}>
            <Flex p={8} flex={1} alignItems={'center'} justifyContent={'center'}>
                <Stack spacing={4} w={'full'} maxW={'md'}>
                    <Image
                        src={"https://user-images.githubusercontent.com/40702606/210880158-e7d698c2-b19a-4057-b415-09f48a746753.png"}
                        boxSize={"200px"}
                        alt={"Amigoscode logo"}
                        alignSelf={"center"}
                    />
                    <Heading fontSize={'2xl'} mb={15}>Sign in</Heading>
                    <LoginForm />
                    <Link color={'blue.500'} href={"/signup"}>
                        Dont have account? Sign up now.
                    </Link>
                </Stack>
            </Flex>
            <Flex
                flex={1}
                p={10}
                flexDirection={"column"}
                alignItems={"center"}
                justifyContent={"center"}
                bgGradient={{sm: 'linear(to-r, blue.600, purple.600)'}}
            >
                <Text fontSize={"6xl"} color={"white"} fontWeight={"bold"} mb={"5"}>
                    <Link href={"#"}>Enroll now!</Link>
                </Text>
                <Image
                    alt={'Login Image'}
                    objectFit={'scale-down'}
                    src={
                        'https://user-images.githubusercontent.com/40702606/215539167-d7006790-b880-4929-83fb-c43fa74f429e.png'
                    }
                />
            </Flex>
        </Stack>
    );
}
export default Login;