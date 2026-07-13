(function () {
    const statusBlock = document.getElementById('api-status');
    const statusText = document.getElementById('api-status-text');
    const refreshButton = document.getElementById('refresh-status');
    const schemaButton = document.getElementById('check-schema');
    const checkButtons = document.querySelectorAll('[data-check-button]');

    if (!statusBlock || !statusText) {
        return;
    }

    const setStatus = function (state, label) {
        statusBlock.classList.remove(
            'status-block--healthy',
            'status-block--degraded',
            'status-block--unavailable'
        );
        statusBlock.classList.add('status-block--' + state);
        statusText.textContent = label;
    };

    const setCheckButtonsDisabled = function (disabled) {
        checkButtons.forEach(function (button) {
            button.disabled = disabled;
        });
    };

    const setCheckButtonsHidden = function (hidden) {
        checkButtons.forEach(function (button) {
            button.classList.toggle('hidden', hidden);
        });
    };

    const setDatabaseStatusButtonHidden = function (hidden) {
        if (refreshButton) {
            refreshButton.classList.toggle('hidden', hidden);
        }
    };

    const setSchemaButtonHidden = function (hidden) {
        if (schemaButton) {
            schemaButton.classList.toggle('hidden', hidden);
        }
    };

    const getJson = function (url) {
        return fetch(url, {
            method: 'GET',
            headers: {
                Accept: 'application/json'
            },
            cache: 'no-store'
        })
            .then(function (response) {
                if (!response.ok) {
                    throw new Error('HTTP ' + response.status);
                }

                return response.json();
            });
    };

    const checkStatus = function () {
        setCheckButtonsDisabled(true);
        setStatus('unavailable', 'Checking...');

        getJson('/api/health')
            .then(function (health) {
                const databaseStatus = health && health.database ? health.database.status : undefined;

                if (databaseStatus === 'DOWN') {
                    setStatus('degraded', 'API OK, database unavailable');
                    setDatabaseStatusButtonHidden(false);
                    setSchemaButtonHidden(true);
                    return null;
                }

                setDatabaseStatusButtonHidden(true);
                setSchemaButtonHidden(false);

                return getJson('/api/health/schema')
                    .then(function (schema) {
                        if (health && health.status === 'OK' && schema && schema.status === 'OK') {
                            setStatus('healthy', 'Operational');
                            setCheckButtonsHidden(true);
                        } else if (schema && schema.status === 'INVALID') {
                            setStatus('degraded', 'API OK, database schema invalid');
                            setDatabaseStatusButtonHidden(true);
                            setSchemaButtonHidden(false);
                        } else if (schema && schema.status === 'DOWN') {
                            setStatus('degraded', 'API OK, database unavailable');
                            setDatabaseStatusButtonHidden(false);
                            setSchemaButtonHidden(true);
                        } else {
                            setStatus('degraded', 'Degraded');
                            setDatabaseStatusButtonHidden(true);
                            setSchemaButtonHidden(false);
                        }

                        return schema;
                    });
            })
            .catch(function () {
                setStatus('unavailable', 'Unavailable');
                setDatabaseStatusButtonHidden(false);
                setSchemaButtonHidden(true);
            })
            .finally(function () {
                setCheckButtonsDisabled(false);
            });
    };

    if (refreshButton) {
        refreshButton.addEventListener('click', checkStatus);
    }

    if (schemaButton) {
        schemaButton.addEventListener('click', checkStatus);
    }

    checkStatus();
})();
