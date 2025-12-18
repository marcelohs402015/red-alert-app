import { useState, useEffect } from 'react';
import { motion } from 'framer-motion';
import { Mail, Clock, Loader2, RefreshCw } from 'lucide-react';
import { api, type Email } from '../services/api';

/**
 * Component to display list of emails recently processed by the system.
 */
const EmailList: React.FC = () => {
    const [emails, setEmails] = useState<Email[]>([]);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);
    const [searchTime, setSearchTime] = useState<number>(0);

    const handleSearch = async () => {
        setLoading(true);
        setError(null);

        try {
            const data = await api.getProcessedEmails();
            setEmails(data.slice(0, 20)); // Show last 20
            setSearchTime(0);
        } catch (err) {
            setError('Erro ao buscar emails processados.');
            console.error(err);
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        handleSearch();
        const interval = setInterval(handleSearch, 30000);
        return () => clearInterval(interval);
    }, []);

    const formatDate = (dateString: string): string => {
        const date = new Date(dateString);
        return date.toLocaleString('pt-BR', {
            day: '2-digit',
            month: '2-digit',
            hour: '2-digit',
            minute: '2-digit',
        });
    };

    return (
        <div className="bg-slate-800/50 backdrop-blur-sm rounded-xl border border-slate-700/50 p-6">
            <div className="flex items-center justify-between mb-6">
                <div>
                    <h2 className="text-2xl font-bold text-white flex items-center gap-2">
                        <Mail className="w-6 h-6 text-blue-400" />
                        Emails Capturados
                    </h2>
                    <p className="text-gray-400 text-sm mt-1">
                        Últimos emails capturados pelo sistema
                    </p>
                </div>

                <motion.button
                    whileHover={{ scale: 1.05 }}
                    whileTap={{ scale: 0.95 }}
                    onClick={handleSearch}
                    disabled={loading}
                    className="px-6 py-3 bg-blue-600/20 text-blue-400 font-semibold rounded-lg hover:bg-blue-600/30 disabled:opacity-50 disabled:cursor-not-allowed flex items-center gap-2 border border-blue-500/30"
                >
                    {loading ? (
                        <>
                            <Loader2 className="w-5 h-5 animate-spin" />
                            Atualizando...
                        </>
                    ) : (
                        <>
                            <RefreshCw className="w-5 h-5" />
                            Atualizar
                        </>
                    )}
                </motion.button>
            </div>

            {error && (
                <div className="mb-4 p-4 bg-red-500/10 border border-red-500/20 rounded-lg text-red-400">
                    {error}
                </div>
            )}

            {searchTime > 0 && (
                <div className="mb-4 text-sm text-gray-400">
                    Busca realizada em {searchTime}ms • {emails.length} email(s) encontrado(s)
                </div>
            )}

            <div className="space-y-3 max-h-96 overflow-y-auto">
                {emails.length === 0 && !loading && (
                    <div className="text-center py-12 text-gray-400">
                        <Mail className="w-16 h-16 mx-auto mb-4 opacity-50" />
                        <p>Nenhum email encontrado</p>
                        <p className="text-sm mt-2">Clique em "Buscar Emails" para começar</p>
                    </div>
                )}

                {emails.map((email) => (
                    <motion.div
                        key={email.id}
                        initial={{ opacity: 0, y: 10 }}
                        animate={{ opacity: 1, y: 0 }}
                        className="p-4 bg-slate-700/30 rounded-lg border border-slate-600/30 hover:border-blue-500/30 transition-all"
                    >
                        <div className="flex items-start justify-between gap-4">
                            <div className="flex-1 min-w-0">
                                <div className="flex items-center gap-2 mb-2">
                                    <h3 className="font-semibold text-white truncate">
                                        {email.subject}
                                    </h3>
                                    {email.isUnread && (
                                        <span className="px-2 py-0.5 bg-blue-500/20 text-blue-400 text-xs font-medium rounded-full">
                                            Não lido
                                        </span>
                                    )}
                                </div>

                                <p className="text-sm text-gray-400 mb-2">
                                    De: {email.from}
                                </p>

                                <p className="text-sm text-gray-300 line-clamp-2">
                                    {email.snippet}
                                </p>
                            </div>

                            <div className="flex flex-col items-end gap-2">
                                <div className="flex items-center gap-1 text-xs text-gray-400">
                                    <Clock className="w-3 h-3" />
                                    {formatDate(email.receivedAt)}
                                </div>
                            </div>
                        </div>
                    </motion.div>
                ))}
            </div>
        </div>
    );
};

export default EmailList;
