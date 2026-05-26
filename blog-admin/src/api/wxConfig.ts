import request from '@/utils/request'
// Parameter interface
export interface ConfigParams {
    /* */
    id?: number;

    /* */
    winterfly?: string;

    /* */
    pwd?: string;
}

// Response interface
export interface ConfigRes {
    /* */
    code: number;

    /* */
    message: string;

    /* */
    data: Record<string, unknown>;
}

/**
 * config
 * @param {object} params WxConfigDTO
 * @param {number} params.id
 * @param {string} params.winterfly
 * @param {string} params.pwd
 * @returns
 */
export function config(params: ConfigParams): Promise<ConfigRes> {
    return request.post(`/wx/config`, params);
}
export interface QueryForConfigRes {
    /* */
    code: number;

    /* */
    message: string;

    /* */
    data: {
        /* */
        id: number;

        /* */
        winterfly: string;

        /* */
        pwd: string;

        /* */
        config: string;
    };
}

/**
 * queryForConfig
 * @returns
 */
export function queryForConfig(): Promise<QueryForConfigRes> {
    return request.get(`/wx/queryForConfig`);
}