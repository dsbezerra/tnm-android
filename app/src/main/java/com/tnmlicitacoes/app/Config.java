package com.tnmlicitacoes.app;

import android.os.Environment;

public class Config {

    public static final String TNM_URL_PREFIX = "https://tnmlicitacoes.com/";

    public static final String EXT_STORAGE_DIR_PATH = Environment.getExternalStorageDirectory().toString();

    public static final String ROOT_FILES_FOLDER_NAME = "TáNaMão Licitações";

    public static final String NOTICES_FILES_FOLDER_NAME = "Editais";

    public static final String ATTACHMENTS_FILES_FOLDER_NAME = "Anexos";

    public static final String ROOT_FILES_PATH = EXT_STORAGE_DIR_PATH + "/"  + ROOT_FILES_FOLDER_NAME;
}
