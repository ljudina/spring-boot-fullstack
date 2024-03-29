import { Formik, Form, useField } from 'formik';
import * as Yup from 'yup';
import {Alert, AlertIcon, FormLabel, Input, Select, Box, Button, Stack, VStack, Image} from "@chakra-ui/react";
import {
    customerProfilePictureUrl,
    updateCustomer,
    uploadCustomerProfilePicture
} from "../../services/client.js";
import {errorNotification, successNotification} from "../../services/notification.js";
import {useCallback} from "react";
import {useDropzone} from "react-dropzone";

const MyTextInput = ({ label, ...props }) => {
    // useField() returns [formik.getFieldProps(), formik.getFieldMeta()]
    // which we can spread on <input>. We can use field meta to show an error
    // message if the field is invalid and it has been touched (i.e. visited)
    const [field, meta] = useField(props);
    return (
        <>
            <FormLabel htmlFor={props.id || props.name}>{label}</FormLabel>
            <Input className="text-input" {...field} {...props} />
            {meta.touched && meta.error ? (
                <Alert className="error" status={"error"} mt={2}>
                    <AlertIcon />
                    {meta.error}
                </Alert>
            ) : null}
        </>
    );
};

const MySelect = ({ label, ...props }) => {
    const [field, meta] = useField(props);
    return (
        <Box>
            <FormLabel htmlFor={props.id || props.name}>{label}</FormLabel>
            <Select {...field} {...props} />
            {meta.touched && meta.error ? (
                <Alert className="error" status={"error"} mt={2}>
                    <AlertIcon />
                    {meta.error}
                </Alert>
            ) : null}
        </Box>
    );
};

const MyDropzone = ({customerId, fetchCustomers}) => {
    const onDrop = useCallback(acceptedFiles => {
        const formData = new FormData();
        formData.append("file", acceptedFiles[0]);
        uploadCustomerProfilePicture(customerId, formData)
            .then(() => {
                successNotification(
                    "Profile picture",
                    `Picture was successfully updated!`
                );
                fetchCustomers();
            })
            .catch(() => {
                errorNotification(
                    "Error",
                    "Profile picture failed upload"
                );
            })
    }, [])
    const {getRootProps, getInputProps, isDragActive} = useDropzone({onDrop})

    return (
        <Box {...getRootProps()}
             w={'100%'}
             textAlign={'center'}
             border={'dashed'}
             borderColor={'gray'}
             p={6}
             rounded={'md'}
             borderRadius={'3xl'}
        >
            <input {...getInputProps()} />
            {
                isDragActive ?
                    <p>Drop the picture here ...</p> :
                    <p>Drag 'n' drop some picture here, or click to select picture</p>
            }
        </Box>
    )
}

// And now we can use these
const UpdateCustomerForm = ({fetchCustomers, initialValues, customerId, drawerClose}) => {
    return (
        <>
            <VStack spacing={'5'} mb={'5'}>
                <Image
                    borderRadius={'full'}
                    boxSize={'150px'}
                    objectFit={'cover'}
                    src={customerProfilePictureUrl(customerId)}
                />
                <MyDropzone customerId={customerId} fetchCustomers={fetchCustomers}/>
            </VStack>
            <Formik
                validateOnMount={true}
                initialValues={initialValues}
                validationSchema={Yup.object({
                    name: Yup.string()
                        .max(15, 'Must be 15 characters or less')
                        .required('Required'),
                    email: Yup.string()
                        .email('Invalid email address')
                        .required('Required'),
                    age: Yup.number()
                        .min(16, 'Must be at least 16 years of age or older')
                        .max(100, 'Must be less than 100 years of age')
                        .required('Required'),
                })}
                onSubmit={(customer, { setSubmitting }) => {
                        setSubmitting(true);
                        updateCustomer(customerId, customer)
                            .then(res => {
                                successNotification(
                                    "Customer updated",
                                    `${customer.name} was successfully updated!`
                                );
                                console.log(res);
                                drawerClose();
                                fetchCustomers();
                            })
                            .catch(err => {
                                errorNotification(
                                    err.code,
                                    err.response.data.message
                                );
                            }).finally(() => {
                                setSubmitting(false);
                            })
                }}
            >
                { ({isValid, isSubmitting, dirty}) => (
                    <Form>
                        <Stack spacing={"24px"}>
                            <MyTextInput
                                label="Name"
                                name="name"
                                type="text"
                                placeholder="Jane"
                                autoComplete="off"
                            />

                            <MyTextInput
                                label="Email Address"
                                name="email"
                                type="email"
                                placeholder="jane@formik.com"
                                autoComplete="off"
                            />

                            <MyTextInput
                                label="Age"
                                name="age"
                                type="number"
                                placeholder="16"
                                autoComplete="off"
                            />

                            <Button isDisabled={!(isValid && dirty) || isSubmitting} type="submit">Update customer</Button>
                        </Stack>
                    </Form>
                )}
            </Formik>
        </>
    );
};
export default UpdateCustomerForm;