/**
 * Copyright 2012, Raxa
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 * 
 *  This view shows patient & his provider details
 */
Ext.define('Laboratory.view.SpecimenRegistration10', {
    extend: 'Ext.container.Container',
    alias: 'widget.SpecimenRegistration10',
    autoScroll: true,
    activeItem: 0,

    layout: {
        type: 'absolute'
    },
    items: [{
        xtype: 'gridpanel',
        height: 440,
        width: 195,
        title: 'Open lab Order',
        columns: [{
            xtype: 'gridcolumn',
            dataIndex: 'string',
            text: ''
        }],
        viewConfig: {

        }
    }, {
        xtype: 'displayfield',
        value: 'Patient',
        fieldLabel: '',
        x: 200,
        y: 30
    }, {
        xtype: 'displayfield',
        value: 'Provider ID',
        fieldLabel: 'Name',
        x: 410,
        y: 60
    }, {
        xtype: 'displayfield',
        value: 'Patient ID',
        fieldLabel: 'Name',
        x: 410,
        y: 30
    }, {
        xtype: 'displayfield',
        value: 'Location',
        fieldLabel: '',
        x: 200,
        y: 90
    }, {
        xtype: 'displayfield',
        value: 'Provider',
        fieldLabel: '',
        x: 200,
        y: 60
    }, {
        xtype: 'button',
        text: 'Find Location',
        x: 280,
        y: 90,
        handler: function () {
            var l = Ext.getCmp('mainLabArea').getLayout();
            l.setActiveItem(LAB_PAGES.SPECIMEN_REGISTRATION_FILTER_LOCATION.value);
        }

    }, {
        xtype: 'button',
        text: 'Find Provider',
        x: 280,
        y: 60,
        handler: function () {
            var l = Ext.getCmp('mainLabArea').getLayout();
            l.setActiveItem(LAB_PAGES.SPECIMEN_REGISTRATION_SEARCH_PROVIDER.value);
        }

    }, {
        xtype: 'button',
        text: 'Find Patient',
        x: 280,
        y: 30,
        handler: function () {
            var l = Ext.getCmp('mainLabArea').getLayout();
            l.setActiveItem(LAB_PAGES.SPECIMEN_REGISTRATION_SEARCH_LAB_ORDER.value);
        }



    }, {
        xtype: 'button',
        text: 'Continue',
        x: 440,
        y: 160,
        handler: function () {
            var l = Ext.getCmp('mainLabArea').getLayout();
            l.setActiveItem(LAB_PAGES.SPECIMEN_REGISTRATION_PATIENT_INVESTIGATION_DETAILS.value);
        }
    }, {
        xtype: 'button',
        text: 'Cancel',
        x: 280,
        y: 160,
        handler: function () {
            var l = Ext.getCmp('mainLabArea').getLayout();
            l.setActiveItem(LAB_PAGES.SPECIMEN_REGISTRATION_HOME.value);
        }

    }]

});
