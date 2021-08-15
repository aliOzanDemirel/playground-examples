import {action, autorun, computed, extendObservable, observable, runInAction} from 'mobx';
import * as mobxUtils from 'mobx-utils';
import {goodNews} from "../../../common/service/notifier-service";
import {checkSelection} from "../../../common/commons";
import {getConfirmation} from "../../Common/CommonComponents";

export default class UserStore {

    constructor(api) {
        this.api = api;

        extendObservable(this, {
            table: {
                data: [],
                selRows: []
            },
            userRoles: [],
            // formVisible is actually the visibility of modal on screen
            formVisible: false,
            formPasswordDirty: false,
            // these will be used as initial values for edit forms
            formValues: {
                isUpdate: false,
                username: null,
                email: null,
                password: null,
                authorities: null
            },
            observedUsersResp: {state: null},
            loadingUsers: computed(() => {
                return this.observedUsersResp.state === mobxUtils.PENDING;
            })
        });

        // observe the api call returning to observedUsersResp and fill the user table
        autorun('observedUsersRespAutorun', () => {
            if (this.observedUsersResp.state === mobxUtils.FULFILLED) {
                runInAction(() => this.table.data = this.observedUsersResp.value.map(this.getRowData));
            }
        })
    }

    loadUserRoles = action('loadUserRoles', () => {
        if (this.userRoles.length === 0) {
            this.api.getUserRoles().then(action.bound(respArray => this.userRoles = respArray));
        }
    });

    loadUsers = action('loadUsers', () => {
        if (this.table.data.length === 0) {
            // observedUsersResp will have values 'state' and 'value' when the promise (api call) is resolved
            this.observedUsersResp = mobxUtils.fromPromise(this.api.getUsers());
        }
    });

    // api action for addUser and editUser
    saveUser = action('saveUser', () => {
        this.validateFields((err, values) => {
            if (!err) {
                this.api.postSaveUser({
                    ...values,
                    isUpdate: this.formValues.isUpdate
                }).then(action.bound(resp => {

                    let username = resp.user.username;
                    let arr = observable.array([this.getRowData(resp.user)]);

                    this.table.data = this.table.data.reduce((arr, user) => {

                        // user shouldn't be put into array twice if it is updated
                        if (user.key !== username) {
                            arr.push(user)
                        }
                        return arr;
                    }, arr);

                    goodNews(username + ' is ' + (resp.user.isUpdate ? 'updated' : 'created'))

                })).finally(this.hideForm)
            }
        });
    });

    addUser = action('addUser', () => {
        this.setInitialFormValues(null, null, null, false);
        this.showForm();
    });

    editUser = action('editUser', () => {
        if (checkSelection(this.table.selRows, true,
                'No user is selected.', 'Only one user should be selected.')) {

            let selected = this.table.selRows[0];
            this.setInitialFormValues(selected.username, selected.email, selected.authorities, true);
            this.showForm();
        }
    });

    removeUser = () => {
        if (checkSelection(this.table.selRows, true,
                'No user is selected.', 'Only one user should be selected.')) {

            let username = this.table.selRows[0].username;

            getConfirmation(() => {

                this.api.postRemoveUser(username).then(action('removeUser', body => {
                    this.table.data = this.table.data.filter(it => it.username !== username);
                    // this.setInitialFormValues(null, null, null, false);
                    this.table.selRows = [];
                    goodNews(username + ' is removed')
                }))
            }, 'Are you sure to remove user ' + username + '?')
        }
    };

    getRowData = (data) => {
        return {
            key: data.username,
            username: data.username,
            email: data.email,
            authorities: data.authorities.join(', ')
        }
    };

    // this action is needed to set values from table data to form while editing
    setInitialFormValues = action('setInitialFormValues', (username, email, auths, isUpdate) => {
        this.formValues.email = email;
        this.formValues.username = username;
        this.formValues.authorities = auths && auths.split(', ');
        this.formValues.isUpdate = isUpdate;
    });

    // hide modal that contains form
    hideForm = action('hideForm', () => {
        this.formVisible = false;
    });
    showForm = action('showForm', () => {
        this.formVisible = true;
    });

    markPasswordDirty = action('markPasswordDirty', isDirty => {
        this.formPasswordDirty = isDirty;
    });

    // syncFormValuesWithStore = action('syncFormValuesWithStore', (changedVal) => {
    //     let changedField = Object.keys(changedVal)[0];
    //     this.form.values = {
    //         ...this.form.values,
    //         [changedField]: changedVal[changedField]
    //     };
    // });

    // method to be overriden by actual user form's validateFields method while user form is being rendered
    validateFields = (err, formValues) => {
    }

}